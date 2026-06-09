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
import java.util.Map;

@RestController
public class OrderControllerImpl implements OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * This is the NEW public endpoint for the "Get a Quote" form.
     * It now calls the new service method to set the correct status.
     */
    @Override
    public ResponseEntity<?> requestPublicQuote(@RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.requestPublicQuote(orderRequestDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequestDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error: " + e.getMessage()));
        }
    }

    // --- NEW ENDPOINTS FOR ADMIN PANEL ---

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllActualOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllActualOrders());
        } catch (Exception e) {
            System.err.println("❌ ERROR in getAllActualOrders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllPublicQuotes() {
        try {
            return ResponseEntity.ok(orderService.getAllPublicQuotes());
        } catch (Exception e) {
            System.err.println("❌ ERROR in getAllPublicQuotes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomer(@PathVariable("customerId") String customerId) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // FIX: Explicit @RequestParam lagaya taaki Spring data mapping me fail na ho
    @Override
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("id") String id,
            @RequestParam("status") OrderStatus status,
            @RequestParam(value = "driverDetails", required = false) String driverDetails) {
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status, driverDetails));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") String id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Guest Order Tracking
    @Override
    public ResponseEntity<?> trackOrder(String orderId, String phone) {
        try {
            return ResponseEntity.ok(orderService.trackPublicOrder(orderId, phone));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}