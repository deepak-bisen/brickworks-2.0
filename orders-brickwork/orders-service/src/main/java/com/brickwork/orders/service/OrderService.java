package com.brickwork.orders.service;



import com.brickwork.orders.dto.OrderDTO;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.entity.Order;

import java.util.List;

public interface OrderService {
   // Order createOrder(OrderRequestDTO orderRequest);

 //   Order createPublicQuote(OrderRequestDTO orderRequest);

  //  Order createOrderWithStatus(OrderRequestDTO orderRequest, String status);

    List<Order> getAllOrders();

    OrderDTO updateOrderStatus(String orderId, String status);

    OrderResponseDTO getOrderById(String orderId);
}
