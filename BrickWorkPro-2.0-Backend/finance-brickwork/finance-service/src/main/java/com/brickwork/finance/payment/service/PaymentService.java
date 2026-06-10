package com.brickwork.finance.payment.service;

import com.brickwork.finance.payment.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.payment.dto.UtrSubmissionDTO;
import com.brickwork.finance.payment.entity.PaymentTransaction;
public interface PaymentService {
    RazorpayOrderResponseDTO createRazorpayOrder(String orderId, Double amount);
    boolean verifyPaymentSignature(String rzpOrderId, String rzpPaymentId, String signature, String orderId);
    void handleWebhookPaymentCaptured(String rzpOrderId, String rzpPaymentId);
    String submitUtrPayment(UtrSubmissionDTO dto);
    String verifyUtrPayment(String orderId, boolean isApproved);
    String selectCashOnDelivery(String orderId, Double amount);
    String confirmCodCollection(String paymentId);
    PaymentTransaction getPaymentDetailsByOrderId(String orderId);
    String initiateRefund(String orderId);
}