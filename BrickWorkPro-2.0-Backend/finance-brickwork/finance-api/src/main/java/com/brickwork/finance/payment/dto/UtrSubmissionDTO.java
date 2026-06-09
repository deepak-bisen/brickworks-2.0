package com.brickwork.finance.payment.dto;

import lombok.Data;

@Data
public class UtrSubmissionDTO {
    private String orderId;
    private String utrNumber;
    private Double amount;
}
