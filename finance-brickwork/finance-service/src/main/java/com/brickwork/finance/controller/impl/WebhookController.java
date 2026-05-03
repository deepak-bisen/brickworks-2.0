package com.brickwork.finance.controller.impl;

import com.brickwork.finance.entity.PaymentTransaction;
import com.brickwork.finance.repository.PaymentTransactionRepository;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/webhooks")
public class WebhookController {

    @Value("${razorpay.webhook.secret}") // Add this to your application.properties!
    private String webhookSecret;

    @Autowired
    private PaymentTransactionRepository transactionRepo;

    // EXTRA PIECE: WEBHOOK ENDPOINT
    // Razorpay sends a POST request here automatically when a payment succeeds.
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            // 1. Verify the webhook is actually from Razorpay (Security!)
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
            }

            JSONObject jsonPayload = new JSONObject(payload);
            String eventName = jsonPayload.getString("event");

            // 2. Handle the "payment.captured" event
            if ("payment.captured".equals(eventName)) {
                JSONObject paymentEntity = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String rzpOrderId = paymentEntity.getString("order_id");
                String rzpPaymentId = paymentEntity.getString("id");

                // Find the transaction in our DB
                transactionRepo.findByRazorpayOrderId(rzpOrderId).ifPresent(transaction -> {
                    // Idempotency: If the UI already updated it, ignore. If the UI crashed, update it now!
                    if (transaction.getStatus() != PaymentTransaction.PaymentStatus.SUCCESS) {
                        transaction.setRazorpayPaymentId(rzpPaymentId);
                        transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
                        transactionRepo.save(transaction);

                        System.out.println("Webhook saved the day! Payment Captured: " + rzpPaymentId);
                        // TODO: OpenFeign call to Orders Service
                    }
                });
            }

            return ResponseEntity.ok("Webhook Processed");

        } catch (Exception e) {
            System.err.println("Webhook Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}