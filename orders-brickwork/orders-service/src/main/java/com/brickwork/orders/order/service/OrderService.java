package com.brickwork.orders.order.service;



import com.brickwork.orders.dto.OrderDTO;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.entity.Order;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);

    OrderResponseDTO requestPublicQuote(OrderRequestDTO orderRequest);

  //  Order createOrderWithStatus(OrderRequestDTO orderRequest, String status);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO updateOrderStatus(String orderId, String status);
    List<OrderResponseDTO> getOrdersByCustomer(String customerId);
    OrderResponseDTO getOrderById(String orderId);
}
