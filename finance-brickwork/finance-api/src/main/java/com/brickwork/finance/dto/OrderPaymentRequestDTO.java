package com.brickwork.finance.dto;

import lombok.Data;

@Data
public class OrderPaymentRequestDTO {
    private String orderId;
    private Double amount;
}