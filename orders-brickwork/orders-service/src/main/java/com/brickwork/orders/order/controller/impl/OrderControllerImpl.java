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
        }catch (IllegalArgumentException e){
            //catching validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }catch (Exception e){
            e.printStackTrace();
            //catching unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error: " + e.getMessage()));
        }
    }


    /**
     * This is the original, PROTECTED endpoint for an admin to create an order.
     * It will require a valid JWT.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        try{
            return ResponseEntity.ok(orderService.createOrder(orderRequestDTO));
        }catch (IllegalArgumentException e){
            //catching validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }catch (Exception e){
            e.printStackTrace();
            //catching unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error: " + e.getMessage()));
        }

    }

    // --- NEW ENDPOINTS FOR ADMIN PANEL ---

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllActualOrders() {
        try{
        return ResponseEntity.ok(orderService.getAllActualOrders());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getAllPublicQuotes() {
        try{
            return ResponseEntity.ok(orderService.getAllPublicQuotes());
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomer(String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @Override
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @Override
    public ResponseEntity<OrderResponseDTO> getOrderById(String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
