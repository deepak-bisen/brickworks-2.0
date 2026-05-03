package com.brickwork.finance.controller.impl;

import com.brickwork.finance.controller.PaymentController;
import com.brickwork.finance.dto.OrderPaymentRequestDTO;
import com.brickwork.finance.dto.PaymentVerificationDTO;
import com.brickwork.finance.dto.RazorpayOrderResponseDTO;
import com.brickwork.finance.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
}