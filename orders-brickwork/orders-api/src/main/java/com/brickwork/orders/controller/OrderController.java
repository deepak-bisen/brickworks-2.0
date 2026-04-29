package com.brickwork.orders.controller;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.entity.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public interface OrderController {

/*
    @PostMapping("/public-quote")
    public ResponseEntity<?> createPublicQuote(@RequestBody OrderRequestDTO orderRequest);
*/
    /**
     * This is the original, PROTECTED endpoint for an admin to create an order.
     * It will require a valid JWT.
     *
    @PostMapping
    public Order CreateOrder(@RequestBody OrderRequestDTO orderRequestDTO);
*/

    @GetMapping("/{id}")
    ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") String id);

    // Added for Phase 2: Status updates (e.g. Pending -> Dispatched)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") String id, @RequestParam("status")  String status);

    // --- NEW ENDPOINTS FOR ADMIN PANEL ---
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders();
}