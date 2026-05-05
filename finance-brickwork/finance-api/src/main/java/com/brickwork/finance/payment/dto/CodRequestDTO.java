package com.brickwork.finance.payment.dto;

import lombok.Data;

@Data
public class CodRequestDTO {
    private String orderId;
    private Double amount;

}