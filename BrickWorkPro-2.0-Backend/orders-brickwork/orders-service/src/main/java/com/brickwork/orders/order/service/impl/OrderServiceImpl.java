package com.brickwork.orders.order.service.impl;

import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.client.ProductionClient;
import com.brickwork.orders.client.ProductionLogFromOrderRequest;
import com.brickwork.orders.notification.service.EmailNotificationService;
import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import com.brickwork.exception.BadRequestException;
import com.brickwork.exception.ForbiddenException;
import com.brickwork.exception.InsufficientStockException;
import com.brickwork.exception.NotFoundException;
import com.brickwork.exception.UnauthorizedException;
import com.brickwork.orders.order.dto.*;
import com.brickwork.orders.order.entity.Order;
import com.brickwork.orders.order.entity.OrderDetails;
import com.brickwork.orders.order.enums.OrderStatus;
import com.brickwork.orders.order.repository.OrderRepository;
import com.brickwork.orders.order.service.OrderService;
import com.brickwork.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final ProductionClient productionClient;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            ProductionClient productionClient,
                            WhatsAppNotificationService whatsappService,
                            EmailNotificationService emailNotificationService) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.productionClient = productionClient;
        this.whatsAppNotificationService = whatsappService;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        String customerId = requestDTO.getCustomerId();

        if (customerId == null || customerId.trim().isEmpty()) {
            if (SecurityUtils.hasRole("CUSTOMER")) {
                customerId = SecurityUtils.getUserId()
                        .orElseThrow(() -> new IllegalArgumentException("Customer identity not available"));
            } else {
                customerId = "GUEST-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            }
            requestDTO.setCustomerId(customerId);
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setCustomerId(requestDTO.getCustomerId());
        order.setCustomerName(requestDTO.getCustomerName());
        order.setCustomerEmail(requestDTO.getCustomerEmail());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCustomerPhone(formatPhoneNumberForWhatsApp(requestDTO.getCustomerPhone()));

        return processOrderItemsAndSave(order, requestDTO);
    }

    @Override
    @Transactional
    public OrderResponseDTO requestPublicQuote(OrderRequestDTO requestDTO) {
        if (requestDTO.getCustomerPhone() == null || requestDTO.getCustomerPhone().length() < 10) {
            throw new IllegalArgumentException("A valid 10-digit phone number is required to request a quote.");
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setCustomerName(requestDTO.getCustomerName());
        order.setCustomerEmail(requestDTO.getCustomerEmail());
        order.setCustomerPhone(requestDTO.getCustomerPhone());
        order.setStatus(OrderStatus.QUOTE_REQUEST);

        return processOrderItemsAndSave(order, requestDTO);
    }

    private OrderResponseDTO processOrderItemsAndSave(Order order, OrderRequestDTO requestDTO) {
        double grossAmount = 0.0;
        double totalAmount = 0.0;
        double totalCost = 0.0;
        double totalDiscount = 0.0;

        List<OrderDetails> detailsList = new ArrayList<>();

        if (requestDTO.getItems() != null) {
            for (OrderItemRequestDTO item : requestDTO.getItems()) {
                ProductDTO product = productClient.getProductById(item.getProductId());

                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Insufficient stock for \"" + product.getName() + "\". " +
                                    "You requested " + item.getQuantity() + " units but only " +
                                    product.getStockQuantity() + " are available."
                    );
                }

                double itemTotal = product.getUnitPrice() * item.getQuantity();
                double itemCost = product.getEstimatedCost() * item.getQuantity();

                grossAmount += itemTotal;

                if (product.getBulkDiscountThreshold() != null && item.getQuantity() >= product.getBulkDiscountThreshold()) {
                    double discount = itemTotal * 0.02;
                    totalDiscount += discount;
                    itemTotal -= discount;
                }

                totalAmount += itemTotal;
                totalCost += itemCost;

                OrderDetails detail = new OrderDetails();
                detail.setOrder(order);
                detail.setProductId(product.getProductId());
                detail.setProductName(product.getName());
                detail.setQuantity(item.getQuantity());
                detail.setPricePerUnit(product.getUnitPrice());
                detailsList.add(detail);
            }
        }

        order.setGrossAmount(grossAmount);
        order.setTotalAmount(totalAmount);
        order.setDiscountApplied(totalDiscount);
        double taxableRevenue = totalAmount / 1.18;
        order.setTotalProfit(taxableRevenue - totalCost);
        order.setOrderDetails(detailsList);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved: id={}, status={}, customerId={}, items={}, total={}",
                savedOrder.getOrderId(), savedOrder.getStatus(), savedOrder.getCustomerId(),
                detailsList.size(), savedOrder.getTotalAmount());

        try {
            String phone = savedOrder.getCustomerPhone() != null ? savedOrder.getCustomerPhone() : "910000000000";
            if (savedOrder.getStatus() == OrderStatus.PAYMENT_RECEIVED || savedOrder.getStatus() == OrderStatus.CONFIRMED_COD) {
                emailNotificationService.sendOrderConfirmationEmail(savedOrder.getCustomerEmail(), savedOrder.getCustomerName(), savedOrder.getOrderId(), savedOrder.getTotalAmount());
                whatsAppNotificationService.sendOrderConfirmation(phone, savedOrder.getOrderId(), savedOrder.getTotalAmount());
            }
        } catch (Exception e) {
            log.error("Order creation notification failed for order {}", savedOrder.getOrderId(), e);
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        validateOrderOwnership(order);
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        validateCustomerAccess(customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateOrderStatus(String orderId, OrderStatus newStatus, String driverDetails) {
        validateStaffStatusChange(newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if ((newStatus == OrderStatus.PAYMENT_RECEIVED || newStatus == OrderStatus.CONFIRMED_COD)
                && (order.getStatus() == OrderStatus.PENDING_PAYMENT)) {
            for (OrderDetails detail : order.getOrderDetails()) {
                productClient.deductStock(detail.getProductId(), detail.getQuantity());
            }
            log.info("Stock deducted for order {}, moving to IN_PRODUCTION", orderId);
            newStatus = OrderStatus.IN_PRODUCTION;

            try {
                ProductionLogFromOrderRequest productionRequest = new ProductionLogFromOrderRequest();
                productionRequest.setOrderId(orderId);
                productionRequest.setItems(order.getOrderDetails().stream()
                        .map(detail -> new ProductionLogFromOrderRequest.ProductionLogFromOrderItem(
                                detail.getProductId(), detail.getQuantity()))
                        .collect(Collectors.toList()));
                productionClient.createFromOrder(productionRequest);
            } catch (Exception e) {
                log.error("Failed to create production logs for order {}", orderId, e);
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("Order status updated: id={}, status={}", orderId, newStatus);

        if ("DISPATCHED".equalsIgnoreCase(String.valueOf(newStatus))) {
            try {
                String phone = formatPhoneNumberForWhatsApp(order.getCustomerPhone());

                String finalDriverDetails = (driverDetails == null || driverDetails.trim().isEmpty())
                        ? "Details will be shared shortly or contact support."
                        : driverDetails;

                whatsAppNotificationService.sendDispatchNotification(phone, orderId, finalDriverDetails);
                emailNotificationService.sendDispatchEmail(order.getCustomerEmail(), order.getCustomerName(), orderId, finalDriverDetails);
            } catch (Exception e) {
                log.error("Dispatch notification failed for order {}", orderId, e);
            }
        }

        return "Order status updated to " + newStatus;
    }

    @Override
    @Transactional
    public List<OrderResponseDTO> getAllActualOrders() {
        return orderRepository.findByStatusNot(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OrderResponseDTO> getAllPublicQuotes() {
        return orderRepository.findByStatus(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO trackPublicOrder(String orderId, String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found! Please check your Order ID."));

        String formattedInputPhone = formatPhoneNumberForWhatsApp(phone);
        String storedPhone = formatPhoneNumberForWhatsApp(order.getCustomerPhone());
        if (order.getCustomerPhone() == null || order.getCustomerPhone().isBlank()
                || !storedPhone.equals(formattedInputPhone)) {
            throw new BadRequestException("Phone number does not match in our records for this order.");
        }

        log.info("Order tracked successfully: orderId={}", orderId);
        return mapToResponse(order);
    }

    private void validateOrderOwnership(Order order) {
        if (SecurityUtils.isAdmin() || SecurityUtils.hasRole("STAFF") || SecurityUtils.hasRole("INTERNAL_SERVICE")) {
            return;
        }
        if (SecurityUtils.hasRole("CUSTOMER")) {
            String userId = SecurityUtils.getUserId()
                    .orElseThrow(() -> new UnauthorizedException("Unauthorized: user identity not available"));
            if (order.getCustomerId() == null || !order.getCustomerId().equals(userId)) {
                throw new ForbiddenException("Access denied: you can only view your own orders");
            }
        }
    }

    private void validateCustomerAccess(String customerId) {
        if (SecurityUtils.isAdmin() || SecurityUtils.hasRole("INTERNAL_SERVICE")) {
            return;
        }
        String userId = SecurityUtils.getUserId()
                .orElseThrow(() -> new UnauthorizedException("Unauthorized: user identity not available"));
        if (!userId.equals(customerId)) {
            throw new ForbiddenException("Access denied: you can only view your own orders");
        }
    }

    private void validateStaffStatusChange(OrderStatus newStatus) {
        if (SecurityUtils.hasRole("STAFF")
                && !SecurityUtils.isAdmin()
                && !SecurityUtils.hasRole("INTERNAL_SERVICE")) {
            if (newStatus != OrderStatus.DISPATCHED && newStatus != OrderStatus.DELIVERED) {
                throw new ForbiddenException("STAFF can only set order status to DISPATCHED or DELIVERED");
            }
        }
    }

    private String formatPhoneNumberForWhatsApp(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "910000000000";
        }

        phone = phone.trim().replaceAll("[^0-9+]", "");

        if (phone.startsWith("+91")) {
            return phone.replace("+", "");
        } else if (phone.startsWith("91") && phone.length() == 12) {
            return phone;
        } else if (phone.startsWith("0") && phone.length() == 11) {
            return "91" + phone.substring(1);
        } else if (phone.length() == 10) {
            return "91" + phone;
        }

        return phone.replace("+", "");
    }

    private OrderResponseDTO mapToResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());

        response.setStatus(order.getStatus() != null ? order.getStatus().toString() : "QUOTE_REQUEST");
        response.setCreatedAt(order.getCreatedAt());
        response.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        response.setDiscountApplied(order.getDiscountApplied() != null ? order.getDiscountApplied() : 0.0);
        response.setNetProfit(order.getTotalProfit() != null ? order.getTotalProfit() : 0.0);
        response.setGrossAmount(order.getGrossAmount() != null && order.getGrossAmount() > 0
                ? order.getGrossAmount()
                : (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setDeliveryAddress(order.getDeliveryAddress());

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<OrderItemResponseDTO> itemDTOs = order.getOrderDetails().stream().map(detail -> {
                OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                itemDTO.setProductId(detail.getProductId());
                itemDTO.setProductName(detail.getProductName());
                itemDTO.setQuantity(detail.getQuantity());
                itemDTO.setUnitPrice(detail.getPricePerUnit());
                return itemDTO;
            }).collect(Collectors.toList());
            response.setItems(itemDTOs);
        }
        return response;
    }
}