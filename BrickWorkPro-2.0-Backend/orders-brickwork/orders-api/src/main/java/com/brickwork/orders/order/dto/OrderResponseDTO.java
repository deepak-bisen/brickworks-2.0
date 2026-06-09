package com.brickwork.orders.order.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private String orderId;
    private String customerId;
    private String status;
    private LocalDateTime createdAt;

    // Customer Details - FIX: Added missing fields for admin dashboard
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;

    // Phase 2 Financials
    private Double totalAmount;   // Amount after discount
    private Double grossAmount; // NEW: Original amount before discount
    private Double discountApplied;
    private Double netProfit;

    // NEW: Items list which will print inside invoice
    private List<OrderItemResponseDTO> items;
}
