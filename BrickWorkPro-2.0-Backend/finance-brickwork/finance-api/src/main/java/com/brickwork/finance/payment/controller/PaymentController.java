package com.brickwork.finance.payment.controller;

import com.brickwork.finance.payment.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/payments")
public interface PaymentController {

    // Customers do payments
    @PostMapping("/create-order")
    ResponseEntity<RazorpayOrderResponseDTO> createPaymentOrder(@RequestBody OrderPaymentRequestDTO request);

    //Admin verifies payments
    @PostMapping("/verify")
    ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationDTO request);

    // Contractor submits UTR
    @PostMapping("/utr/submit")
    public ResponseEntity<String> submitUtr(@RequestBody UtrSubmissionDTO dto);

    // Admin Approves/Rejects
    @PostMapping("/utr/verify/{orderId}")
    public ResponseEntity<String> verifyUtr(@PathVariable String orderId, @RequestParam boolean approved);

    // Endpoint for the Customer/Frontend to choose COD
    @PostMapping("/cod/select")
    public ResponseEntity<String> selectCod(@RequestBody CodRequestDTO dto);

    // Endpoint for the Admin to click "Cash Received"
    @PostMapping("/cod/collect/{paymentId}")
    public ResponseEntity<String> collectCodCash(@PathVariable String paymentId);

    // NAYA ROUTE: Get Payment details
    @GetMapping("/order/{orderId}")
    ResponseEntity<?> getPaymentDetailsByOrderId(@PathVariable("orderId") String orderId);

    @PostMapping("/refund/{orderId}")
    // @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<String> initiateRefund(@PathVariable("orderId") String orderId);
}