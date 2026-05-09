package com.brickwork.finance.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RazorpayOrderResponseDTO {
    private String razorpayOrderId;
    private Double amount;
    private String currency;
}