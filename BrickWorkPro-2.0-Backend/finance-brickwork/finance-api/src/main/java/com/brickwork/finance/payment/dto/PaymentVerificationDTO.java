package com.brickwork.finance.payment.dto;

import lombok.Data;

@Data
public class PaymentVerificationDTO {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String orderId;
}