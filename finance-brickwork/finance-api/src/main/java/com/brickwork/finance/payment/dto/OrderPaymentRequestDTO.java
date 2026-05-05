package com.brickwork.finance.payment.dto;

import lombok.Data;

@Data
public class OrderPaymentRequestDTO {
    private String orderId;
    private Double amount;
}