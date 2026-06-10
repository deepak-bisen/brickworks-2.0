package com.brickwork.finance.payment.controller.impl;

import com.brickwork.finance.payment.controller.PaymentController;
import com.brickwork.finance.payment.dto.*;
import com.brickwork.finance.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentControllerImpl implements PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentControllerImpl.class);

    @Autowired
    private PaymentService paymentService;

    @Override
    public ResponseEntity<RazorpayOrderResponseDTO> createPaymentOrder(OrderPaymentRequestDTO request) {
        try {
            return ResponseEntity.ok(paymentService.createRazorpayOrder(request.getOrderId(), request.getAmount()));
        } catch (Exception e) {
            logger.error("Error creating Razorpay order for orderId: " + request.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
    //@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<String> submitUtr(@RequestBody UtrSubmissionDTO dto) {
        return ResponseEntity.ok(paymentService.submitUtrPayment(dto));
    }

    // Admin Approves/Rejects
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> verifyUtr(@PathVariable String orderId, @RequestParam boolean approved) {
        return ResponseEntity.ok(paymentService.verifyUtrPayment(orderId, approved));
    }

    // Endpoint for the Customer/Frontend to choose COD
    @Override
    //@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
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

    // NAYA ENDPOINT: Admin UTR modal ke liye data fetch karne ke liye
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> getPaymentDetailsByOrderId(@PathVariable("orderId") String orderId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentDetailsByOrderId(orderId));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment details not found");
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initiateRefund(@PathVariable("orderId") String orderId) {
        try {
            return ResponseEntity.ok(paymentService.initiateRefund(orderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}