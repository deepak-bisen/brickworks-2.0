package com.brickwork.finance.payment.controller.impl;

import com.brickwork.finance.payment.controller.PaymentController;
import com.brickwork.finance.payment.dto.*;
import com.brickwork.finance.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentControllerImpl implements PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Override
    public ResponseEntity<RazorpayOrderResponseDTO> createPaymentOrder(OrderPaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request.getOrderId(), request.getAmount()));
    }

    @Override
    public ResponseEntity<String> verifyPayment(PaymentVerificationDTO request) {
        boolean isValid = paymentService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getOrderId()
        );

        if (isValid) {
            return ResponseEntity.ok("Payment Verified and Order Updated");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Payment Signature");
    }

    @Override
    public ResponseEntity<String> submitUtr(@RequestBody UtrSubmissionDTO dto) {
        return ResponseEntity.ok(paymentService.submitUtrPayment(dto));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyUtr(@PathVariable String orderId, @RequestParam boolean approved) {
        return ResponseEntity.ok(paymentService.verifyUtrPayment(orderId, approved));
    }

    @Override
    public ResponseEntity<String> selectCod(@RequestBody CodRequestDTO dto) {
        return ResponseEntity.ok(paymentService.selectCashOnDelivery(dto.getOrderId(), dto.getAmount()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> collectCodCash(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.confirmCodCollection(paymentId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> getPaymentDetailsByOrderId(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentDetailsByOrderId(orderId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initiateRefund(@PathVariable("orderId") String orderId) {
        return ResponseEntity.ok(paymentService.initiateRefund(orderId));
    }
}