package com.brickwork.orders.controller.impl;

import com.brickwork.orders.controller.OrderController;
import com.brickwork.orders.dto.OrderDTO;
import com.brickwork.orders.dto.OrderRequestDTO;
import com.brickwork.orders.dto.OrderResponseDTO;
import com.brickwork.orders.entity.Order;
import com.brickwork.orders.service.OrderService;
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
     *
    @PostMapping("/public-quote")
    public ResponseEntity<?> createPublicQuote(@RequestBody OrderRequestDTO orderRequest) {
        try {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createPublicQuote(orderRequest));
        }catch (IllegalArgumentException e){
            //catching validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }catch (Exception e){
            //catching unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occured. Please try again later."));
        }
    }
*/

    /**
     * This is the original, PROTECTED endpoint for an admin to create an order.
     * It will require a valid JWT.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponseDTO> CreateOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderRequestDTO));
    }

    // --- NEW ENDPOINTS FOR ADMIN PANEL ---

//    @GetMapping
//    public ResponseEntity<List<OrderDTO>> getAllOrders() {
//        try{
//        return ResponseEntity.ok(orderService.getAllOrders());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @Override
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomer(String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @Override
    public ResponseEntity<OrderResponseDTO> getOrderById(String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
