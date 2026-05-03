package com.brickwork.finance.controller;

import com.brickwork.finance.dto.OrderPaymentRequestDTO;
import com.brickwork.finance.dto.PaymentVerificationDTO;
import com.brickwork.finance.dto.RazorpayOrderResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/finance/payments")
public interface PaymentController {

    @PostMapping("/create-order")
    ResponseEntity<RazorpayOrderResponseDTO> createPaymentOrder(@RequestBody OrderPaymentRequestDTO request);

    @PostMapping("/verify")
    ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationDTO request);
}