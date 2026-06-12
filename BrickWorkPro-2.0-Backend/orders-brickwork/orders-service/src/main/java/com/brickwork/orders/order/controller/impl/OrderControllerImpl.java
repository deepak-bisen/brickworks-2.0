package com.brickwork.orders.order.controller.impl;

import com.brickwork.orders.order.controller.OrderController;
import com.brickwork.orders.order.dto.OrderRequestDTO;
import com.brickwork.orders.order.dto.OrderResponseDTO;
import com.brickwork.orders.order.enums.OrderStatus;
import com.brickwork.orders.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderControllerImpl implements OrderController {

    @Autowired
    private OrderService orderService;

    @Override
    public ResponseEntity<?> requestPublicQuote(@RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.requestPublicQuote(orderRequestDTO));
    }

    @Override
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllActualOrders() {
        return ResponseEntity.ok(orderService.getAllActualOrders());
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllPublicQuotes() {
        return ResponseEntity.ok(orderService.getAllPublicQuotes());
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomer(@PathVariable("customerId") String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @Override
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("id") String id,
            @RequestParam("status") OrderStatus status,
            @RequestParam(value = "driverDetails", required = false) String driverDetails,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, driverDetails, paymentMethod));
    }

    @Override
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Override
    public ResponseEntity<OrderResponseDTO> trackOrder(String orderId, String phone) {
        return ResponseEntity.ok(orderService.trackPublicOrder(orderId, phone));
    }

    @Override
    public ResponseEntity<?> resendNotifications(@PathVariable("id") String id, @RequestParam(value = "driverDetails", required = false) String driverDetails) {
        orderService.resendNotifications(id, driverDetails);
        return ResponseEntity.ok("Notifications resent for order " + id + ". Check logs for delivery status.");
    }
}