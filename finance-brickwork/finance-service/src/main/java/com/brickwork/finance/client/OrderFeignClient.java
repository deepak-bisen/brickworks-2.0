package com.brickwork.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

// Connects to the orders-brickwork service via Eureka
@FeignClient(name = "orders-service", path = "/api/orders")
public interface OrderFeignClient {

    // Fetch order details dynamically as a Map to populate the PDF
    @GetMapping("/{orderId}")
    Map<String, Object> getOrderById(@PathVariable("orderId") String orderId);

    // Update the status in the orders microservice when payment succeeds
    @PutMapping("/{orderId}/status")
    void updateOrderStatus(@PathVariable("orderId") String orderId, @RequestParam("status") String status);
}