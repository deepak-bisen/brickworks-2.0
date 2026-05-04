package com.brickwork.finance.controller.impl;

import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/webhooks")
public class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    // We inject your PaymentService to actually update the DB once verified
    // @Autowired
    // private PaymentService paymentService;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            // 1. Verify the signature using the Razorpay SDK
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (!isValid) {
                System.err.println("Webhook signature verification failed!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Signature");
            }

            // 2. Parse the payload to figure out what happened
            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            System.out.println("Received verified Razorpay Webhook Event: " + eventType);

            // 3. Handle the specific event
            if ("order.paid".equals(eventType)) {
                JSONObject orderEntity = event.getJSONObject("payload").getJSONObject("order").getJSONObject("entity");
                String rzpOrderId = orderEntity.getString("id");

                // Extract receipt (which usually contains your internal orderId if you passed it during creation)
                String internalOrderId = orderEntity.getString("receipt");

                System.out.println("Order Paid! Razorpay Order ID: " + rzpOrderId);

                // TODO: Call your paymentService here to update the transaction status to SUCCESS
                // paymentService.processSuccessfulWebhookPayment(rzpOrderId, internalOrderId);
            }
            else if ("payment.failed".equals(eventType)) {
                System.out.println("Payment Failed Event Received.");
                // TODO: Handle failure logic
            }

            // 4. ALWAYS return a 200 OK to Razorpay so they know you received it.
            // If you don't return 200, Razorpay will keep retrying the webhook!
            return ResponseEntity.ok("Webhook Received");

        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
}