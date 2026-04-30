package com.brickwork.orders.controller;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.enums.OrderStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public interface OrderController {


    @PostMapping("/public-quote")
    public ResponseEntity<?> requestPublicQuote(@RequestBody OrderRequestDTO orderRequest);

    /**
     * This is the original, PROTECTED endpoint for an admin to create an order.
     * It will require a valid JWT.
     */
    @PostMapping("/create")
    ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO);

    @GetMapping("/{id}")
    ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") String id);

    // Endpoint for the secure Customer Dashboard
    // Added for Phase 2: Status updates (e.g. Pending -> Dispatched)
    @PutMapping("/{id}/status")
    ResponseEntity<?> updateOrderStatus(@PathVariable("id") String id, @RequestParam("status") OrderStatus status);

    // RESTORED PHASE 1: Endpoint for the secure Customer Dashboard
    @GetMapping("/customer/{customerId}")
    ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomer(@PathVariable("customerId") String customerId);

    // --- NEW ENDPOINTS FOR ADMIN PANEL ---
    @GetMapping("/all/orders")
    ResponseEntity<List<OrderResponseDTO>> getAllActualOrders();

    @GetMapping("/all/get/public-qoute")
    ResponseEntity<List<OrderResponseDTO>> getAllPublicQuotes();
}