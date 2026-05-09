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
        try {
            return ResponseEntity.ok(paymentService.createRazorpayOrder(request.getOrderId(), request.getAmount()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Payment Signature");
        }
    }


    // Contractor submits UTR
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> submitUtr(@RequestBody UtrSubmissionDTO dto) {
        return ResponseEntity.ok(paymentService.submitUtrPayment(dto));
    }

    // Admin Approves/Rejects
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyUtr(@PathVariable String paymentId, @RequestParam boolean approved) {
        return ResponseEntity.ok(paymentService.verifyUtrPayment(paymentId, approved));
    }

    // Endpoint for the Customer/Frontend to choose COD
    @Override
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
    public ResponseEntity<String> selectCod(@RequestBody CodRequestDTO dto) {
        String response = paymentService.selectCashOnDelivery(dto.getOrderId(), dto.getAmount());
        return ResponseEntity.ok(response);
    }

    // Endpoint for the Admin to click "Cash Received"
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> collectCodCash(@PathVariable String paymentId) {
        String response = paymentService.confirmCodCollection(paymentId);
        return ResponseEntity.ok(response);
    }
}