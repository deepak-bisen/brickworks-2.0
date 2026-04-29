package com.brickwork.orders.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {
    private String orderId;
    private String customerId;
    private String status;
    private LocalDateTime orderDate;

    // Phase 2 Financials
    private Double totalAmount;
    private Double discountApplied;
    private Double netProfit;
}
