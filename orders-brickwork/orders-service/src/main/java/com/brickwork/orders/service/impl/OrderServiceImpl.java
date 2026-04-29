package com.brickwork.orders.service.impl;

import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.dto.OrderItemRequestDTO;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.dto.ProductDTO;
import com.brickwork.orders.entity.Order;
import com.brickwork.orders.entity.OrderDetails;
import com.brickwork.orders.repository.OrderRepository;
import com.brickwork.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Order order = new Order();
        order.setCustomerId(requestDTO.getCustomerId());
        order.setDeliveryAddress(requestDTO.getDeliveryAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING_PAYMENT");

        double totalAmount = 0.0;
        double totalCost = 0.0;
        double totalDiscount = 0.0;

        List<OrderDetails> detailsList = new ArrayList<>();

        for (OrderItemRequestDTO item : requestDTO.getItems()) {

            ProductDTO product = productClient.getProductById(item.getProductId());

            double itemTotal = product.getUnitPrice() * item.getQuantity();
            double itemCost = product.getEstimatedCost() * item.getQuantity();

            if (product.getBulkDiscountThreshold() != null && item.getQuantity() >= product.getBulkDiscountThreshold()) {
                double discount = itemTotal * 0.05;
                totalDiscount += discount;
                itemTotal -= discount;
            }

            totalAmount += itemTotal;
            totalCost += itemCost;

            // NEW: Create the OrderDetail record
            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProductId(product.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setPricePerUnit(product.getUnitPrice());
            detailsList.add(detail);
        }

        double netProfit = totalAmount - totalCost;

        order.setTotalAmount(totalAmount);
        order.setDiscountApplied(totalDiscount);
        order.setTotalProfit(netProfit);
        order.setOrderDetails(detailsList); // Link items to order

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
    public OrderResponseDTO updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
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