package com.brickwork.finance.payment.service;

import com.brickwork.finance.payment.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.payment.dto.UtrSubmissionDTO;
import com.razorpay.RazorpayException;

public interface PaymentService {
    RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount) throws RazorpayException;
    boolean verifyPaymentSignature(String rzpOrderId, String rzpPaymentId, String signature, String orderId);
    void handleWebhookPaymentCaptured(String rzpOrderId, String rzpPaymentId);
    String submitUtrPayment(UtrSubmissionDTO dto);
    String verifyUtrPayment(String paymentId, boolean isApproved);
    String selectCashOnDelivery(String orderId, Double amount);
    String confirmCodCollection(String paymentId);
}