package com.brickwork.orders.order.service.impl;

import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import com.brickwork.orders.order.dto.OrderItemRequestDTO;
import com.brickwork.orders.order.dto.OrderRequestDTO;
import com.brickwork.orders.order.dto.OrderResponseDTO;
import com.brickwork.orders.order.dto.ProductDTO;
import com.brickwork.orders.order.entity.Order;
import com.brickwork.orders.order.entity.OrderDetails;
import com.brickwork.orders.order.enums.OrderStatus;
import com.brickwork.orders.order.repository.OrderRepository;
import com.brickwork.orders.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private WhatsAppNotificationService whatsAppNotificationService;


    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        // Strict validation for formal orders
        if (requestDTO.getCustomerId() == null || requestDTO.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required to create an order.");
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setCustomerId(requestDTO.getCustomerId());
        order.setGuestName(requestDTO.getGuestName());
        order.setGuestEmail(requestDTO.getGuestEmail());
        order.setGuestPhone(requestDTO.getGuestPhone());
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        return processOrderItemsAndSave(order, requestDTO);
    }

    @Override
    public OrderResponseDTO requestPublicQuote(OrderRequestDTO requestDTO) {
        // Strict validation for public leads
        if (requestDTO.getGuestPhone() == null || requestDTO.getGuestPhone().length() < 10) {
            throw new IllegalArgumentException("A valid 10-digit phone number is required to request a quote.");
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setGuestName(requestDTO.getGuestName());
        order.setGuestEmail(requestDTO.getGuestEmail());
        order.setGuestPhone(requestDTO.getGuestPhone());
        order.setStatus(OrderStatus.QUOTE_REQUEST);

        return processOrderItemsAndSave(order, requestDTO);
    }

    // Helper method to process items, apply discounts, calculate profit, and save
    private OrderResponseDTO processOrderItemsAndSave(Order order, OrderRequestDTO requestDTO) {
        double totalAmount = 0.0;
        double totalCost = 0.0;
        double totalDiscount = 0.0;

        List<OrderDetails> detailsList = new ArrayList<>();

        if (requestDTO.getItems() != null) {
            for (OrderItemRequestDTO item : requestDTO.getItems()) {
                ProductDTO product = productClient.getProductById(item.getProductId());

                double itemTotal = product.getUnitPrice() * item.getQuantity();
                double itemCost = product.getEstimatedCost() * item.getQuantity();

                // Apply Bulk Discount threshold logic
                if (product.getBulkDiscountThreshold() != null && item.getQuantity() >= product.getBulkDiscountThreshold()) {
                    double discount = itemTotal * 0.05; // 5% bulk discount
                    totalDiscount += discount;
                    itemTotal -= discount;
                }

                totalAmount += itemTotal;
                totalCost += itemCost;

                OrderDetails detail = new OrderDetails();
                detail.setOrder(order);
                detail.setProductId(product.getProductId());
                detail.setQuantity(item.getQuantity());
                detail.setPricePerUnit(product.getUnitPrice());
                detailsList.add(detail);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountApplied(totalDiscount);
        order.setTotalProfit(totalAmount - totalCost);
        order.setOrderDetails(detailsList);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // --- NEW: Deduct stock when the order leaves! ---
        if (newStatus == OrderStatus.DISPATCHED && order.getStatus() != OrderStatus.DISPATCHED) {
            for (OrderDetails detail : order.getOrderDetails()) {
                productClient.deductStock(detail.getProductId(), detail.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        // --- THE WHATSAPP TRIGGER ---
        if ("DISPATCHED".equalsIgnoreCase(String.valueOf(newStatus))) {
            // Assume order has a getGuestPhone() or you fetch customer phone
            String phone = order.getGuestPhone() != null ? order.getGuestPhone() : "+910000000000";
            whatsAppNotificationService.sendDispatchNotification(phone, orderId);
        }

        return "Order status updated to " + newStatus;
    }


    @Override
    public List<OrderResponseDTO> getAllActualOrders() {
        // Strict Filter: Fetch everything that is NOT a quote
        return orderRepository.findByStatusNot(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getAllPublicQuotes() {
        // Strict Filter: Fetch ONLY quotes
        return orderRepository.findByStatus(OrderStatus.QUOTE_REQUEST)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus().toString());
        response.setCreatedAt(order.getCreatedAt());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountApplied(order.getDiscountApplied());
        response.setNetProfit(order.getTotalProfit());
        return response;
    }
}