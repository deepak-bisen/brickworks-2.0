package com.brickwork.orders.service.impl;

import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.dto.*;
import com.brickwork.orders.entity.Order;
import com.brickwork.orders.entity.OrderDetails;
import com.brickwork.orders.repository.OrderRepository;
import com.brickwork.orders.service.OrderService;
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

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {

        // RESTORED PHASE 1 VALIDATION: Ensure public leads provide valid contact info
        boolean isGuest = requestDTO.getCustomerId() == null || requestDTO.getCustomerId().trim().isEmpty();

        if (isGuest && (requestDTO.getGuestPhone() == null || requestDTO.getGuestPhone().length() < 10)) {
            throw new IllegalArgumentException("A valid 10-digit phone number is required to request a quote.");
        }

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());

        // Differentiate between logged-in Contractor and Public Guest (FUNC-005)
        if (!isGuest) {
            order.setCustomerId(requestDTO.getCustomerId());
            order.setStatus("PENDING_PAYMENT");
        } else {
            order.setGuestName(requestDTO.getGuestName());
            order.setGuestEmail(requestDTO.getGuestEmail());
            order.setGuestPhone(requestDTO.getGuestPhone());
            order.setStatus("QUOTE_REQUEST");
        }

        double totalAmount = 0.0;
        double totalCost = 0.0;
        double totalDiscount = 0.0;

        List<OrderDetails> detailsList = new ArrayList<>();

        for (OrderItemRequestDTO item : requestDTO.getItems()) {
            // Fetch live product data via Feign
            ProductDTO product = productClient.getProductById(item.getProductId());

            double itemTotal = product.getUnitPrice() * item.getQuantity();
            double itemCost = product.getEstimatedCost() * item.getQuantity();

            // Phase 2: Apply Bulk Discount logic
            if (product.getBulkDiscountThreshold() != null && item.getQuantity() >= product.getBulkDiscountThreshold()) {
                double discount = itemTotal * 0.05;
                totalDiscount += discount;
                itemTotal -= discount;
            }

            totalAmount += itemTotal;
            totalCost += itemCost;

            // Create Line Items
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProductId(product.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setPricePerUnit(product.getUnitPrice());
            detailsList.add(detail);
        }

        // Phase 2: Save Financials
        order.setTotalAmount(totalAmount);
        order.setDiscountApplied(totalDiscount);
        order.setTotalProfit(totalAmount - totalCost);
        order.setOrderDetails(detailsList);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

//    @Override
//    public List<OrderDTO> getAllOrders() {
//        return mapToResponse(orderRepository.findAll());
//    }

    @Override
    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    // RESTORED PHASE 1: Implementation for Customer Portal
    @Override
    public List<OrderResponseDTO> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        // TODO: Future trigger for Notification Service (FUNC-012)

        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponseDTO mapToResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountApplied(order.getDiscountApplied());
        response.setNetProfit(order.getTotalProfit());
        return response;
    }
}