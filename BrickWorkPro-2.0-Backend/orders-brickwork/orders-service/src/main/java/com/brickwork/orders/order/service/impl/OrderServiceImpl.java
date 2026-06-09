package com.brickwork.orders.order.service.impl;

import com.brickwork.orders.client.FinanceClient;
import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.notification.service.EmailNotificationService;
import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import com.brickwork.orders.order.dto.*;
import com.brickwork.orders.order.entity.Order;
import com.brickwork.orders.order.entity.OrderDetails;
import com.brickwork.orders.order.enums.OrderStatus;
import com.brickwork.orders.order.repository.OrderRepository;
import com.brickwork.orders.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final FinanceClient financeClient;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            WhatsAppNotificationService whatsappService,
                            FinanceClient financeClient, EmailNotificationService emailNotificationService) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.whatsAppNotificationService = whatsappService;
        this.financeClient = financeClient;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        // Strict validation for formal orders
        /**
        if (requestDTO.getCustomerId() == null || requestDTO.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required to create an order.");
        }*/

        // --- NEW GUEST CHECKOUT LOGIC ---
        String customerId = requestDTO.getCustomerId();

        // If there is no Customer ID, they are a guest! Assigning a unique guest tag.
        if (customerId == null || customerId.trim().isEmpty()) {
            customerId = "GUEST-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            requestDTO.setCustomerId(customerId); // Injecting it so the rest of code doesn't break
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
        // Strict validation for public leads
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

    // Helper method to process items, apply discounts, calculate profit, and save
    private OrderResponseDTO processOrderItemsAndSave(Order order, OrderRequestDTO requestDTO) {
        double grossAmount = 0.0; // amount before discount
        double totalAmount = 0.0; // amount after discount
        double totalCost = 0.0;
        double totalDiscount = 0.0;

        List<OrderDetails> detailsList = new ArrayList<>();

        if (requestDTO.getItems() != null) {
            for (OrderItemRequestDTO item : requestDTO.getItems()) {
                ProductDTO product = productClient.getProductById(item.getProductId());

                // STRICT INVENTORY CHECK LOGIC
                // ==========================================
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException(
                            "Order Failed: Insufficient stock for product '" + product.getName() + "'. " +
                                    "You requested " + item.getQuantity() + " but only " + product.getStockQuantity() + " are available. " +
                                    "Once the stock is available, you will be notified."
                    );
                }

                double itemTotal = product.getUnitPrice() * item.getQuantity();
                double itemCost = product.getEstimatedCost() * item.getQuantity();

                grossAmount += itemTotal; // Save Gross before discount

                // Apply Bulk Discount threshold logic
                if (product.getBulkDiscountThreshold() != null && item.getQuantity() >= product.getBulkDiscountThreshold()) {
                    double discount = itemTotal * 0.02; // 2% bulk discount
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
//        // 🚀 MAGIC FIX: Extract 18% GST before calculating Net Profit
        double taxableRevenue = totalAmount / 1.18; // Ye actual paisa hai jo factory ko mila
        order.setTotalProfit(taxableRevenue - totalCost); // Sahi Profit (Bina Tax ke)
//        order.setTotalProfit(totalAmount - totalCost);
        order.setOrderDetails(detailsList);

        Order savedOrder = orderRepository.save(order);

        // --- BLOCK 1: NOTIFICATIONS (Separate Try-Catch) ---
        try{
            // trigger notification
            String phone = savedOrder.getCustomerPhone() != null ? savedOrder.getCustomerPhone() : "910000000000";
            // Only send confirmation if it's an actual order (not just a quote request)
            if (savedOrder.getStatus() == OrderStatus.PAYMENT_RECEIVED || savedOrder.getStatus() == OrderStatus.CONFIRMED_COD) {
                // Email Fire
                emailNotificationService.sendOrderConfirmationEmail(savedOrder.getCustomerEmail(), savedOrder.getCustomerName(), savedOrder.getOrderId(), savedOrder.getTotalAmount());
                // WhatsApp Fire
                whatsAppNotificationService.sendOrderConfirmation(phone, savedOrder.getOrderId(), savedOrder.getTotalAmount());
            }
        } catch (Exception e) {
            System.err.println("Order Creation Notification Failed: " + e.getMessage());
        }

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateOrderStatus(String orderId, OrderStatus newStatus, String driverDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🚀 MAGIC FIX: Deduct stock when the order is successfully CONFIRMED!
        // (Either via Online Payment, UTR Approval, or COD Selection)
        if ((newStatus == OrderStatus.PAYMENT_RECEIVED || newStatus == OrderStatus.CONFIRMED_COD)
                && (order.getStatus() == OrderStatus.PENDING_PAYMENT)) {
            for (OrderDetails detail : order.getOrderDetails()) {
                productClient.deductStock(detail.getProductId(), detail.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        // --- THE WHATSAPP & EMAIL TRIGGER ---
        if ("DISPATCHED".equalsIgnoreCase(String.valueOf(newStatus))) {
            try {
                String phone = formatPhoneNumberForWhatsApp(order.getCustomerPhone());

                String finalDriverDetails = (driverDetails == null || driverDetails.trim().isEmpty())
                        ? "Details will be shared shortly or contact support."
                        : driverDetails;

                // Trigger WhatsApp
                whatsAppNotificationService.sendDispatchNotification(phone, orderId, finalDriverDetails);
                // Trigger Email
                emailNotificationService.sendDispatchEmail(order.getCustomerEmail(), order.getCustomerName(), orderId, finalDriverDetails);
            } catch (Exception e) {
                System.err.println("Dispatch Notification Failed: " + e.getMessage());
            }
        }

        return "Order status updated to " + newStatus;
    }


    @Override
    @Transactional
    public List<OrderResponseDTO> getAllActualOrders() {
        // Strict Filter: Fetch everything that is NOT a quote
        return orderRepository.findByStatusNot(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OrderResponseDTO> getAllPublicQuotes() {
        // Strict Filter: Fetch ONLY quotes
        return orderRepository.findByStatus(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 🚀 NEW: Secure Guest Order Tracking Logic
    @Override
    @Transactional
    public OrderResponseDTO trackPublicOrder(String orderId, String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found! Please check your Order ID."));

        // Security Check: Phone number should be match so anyone else can't see tracking details
        String formattedInputPhone = formatPhoneNumberForWhatsApp(phone);
        if (order.getCustomerPhone() == null || !order.getCustomerPhone().equals(formattedInputPhone)) {
            throw new RuntimeException("Phone number does not match in our recordsfor this order.");
        }

        return mapToResponse(order);
    }

    // 🚀 MAGIC FIX: Centralized WhatsApp Number Formatter
    private String formatPhoneNumberForWhatsApp(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "910000000000"; // Safe fallback
        }

        // Step 1: Remove all spaces, dashes, brackets (Keep only numbers and '+')
        phone = phone.trim().replaceAll("[^0-9+]", "");

        // Step 2: Format to strict 91-XXXXXXXXXX format
        if (phone.startsWith("+91")) {
            return phone.replace("+", ""); // e.g. +919876543210 -> 919876543210
        } else if (phone.startsWith("91") && phone.length() == 12) {
            return phone; // Already correct
        } else if (phone.startsWith("0") && phone.length() == 11) {
            return "91" + phone.substring(1); // e.g. 09876543210 -> 919876543210
        } else if (phone.length() == 10) {
            return "91" + phone; // e.g. 9876543210 -> 919876543210
        }

        // Default fallback agar kisi ne invalid length daali hai par system crash na ho
        return phone.replace("+", "");
    }

    private OrderResponseDTO mapToResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());

        // SAFELY HANDLE NULL VALUES:
        response.setStatus(order.getStatus() != null ? order.getStatus().toString() : "QUOTE_REQUEST");
        response.setCreatedAt(order.getCreatedAt());
        response.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        response.setDiscountApplied(order.getDiscountApplied() != null ? order.getDiscountApplied() : 0.0);
        response.setNetProfit(order.getTotalProfit() != null ? order.getTotalProfit() : 0.0);
        // FIX: Sahi variable map karna hai, aur purane orders ke liye TotalAmount fallback dena hai taaki 0 na dikhe
        response.setGrossAmount(order.getGrossAmount() != null && order.getGrossAmount() > 0
                ? order.getGrossAmount()
                : (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setDeliveryAddress(order.getDeliveryAddress());

        // 🚀 NAYA: OrderDetails ko DTO mein map karke bhej rahe hain taaki invoice print ho sake
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