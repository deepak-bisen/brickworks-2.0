package com.brickwork.finance.service;

import com.brickwork.finance.dto.RazorpayOrderResponseDTO;
import com.razorpay.RazorpayException;

public interface PaymentService {
    RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) throws RazorpayException;
    boolean verifyPaymentSignature(String rzpOrderId, String rzpPaymentId, String signature, String orderId);
    void handleWebhookPaymentCaptured(String rzpOrderId, String rzpPaymentId);
}