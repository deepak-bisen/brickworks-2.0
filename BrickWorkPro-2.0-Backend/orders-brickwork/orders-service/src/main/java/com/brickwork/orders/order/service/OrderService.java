package com.brickwork.orders.order.service;



import com.brickwork.orders.order.dto.OrderRequestDTO;
import com.brickwork.orders.order.dto.OrderResponseDTO;
import com.brickwork.orders.order.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);

    OrderResponseDTO requestPublicQuote(OrderRequestDTO orderRequest);

  //  Order createOrderWithStatus(OrderRequestDTO orderRequest, String status);


    String updateOrderStatus(String orderId, OrderStatus status, String driverDetails, String paymentMethod);
    List<OrderResponseDTO> getOrdersByCustomer(String customerId);
    OrderResponseDTO getOrderById(String orderId);

    List<OrderResponseDTO> getAllActualOrders();

    List<OrderResponseDTO> getAllPublicQuotes();

    OrderResponseDTO trackPublicOrder(String orderId, String phone);

    void resendNotifications(String orderId);
}
