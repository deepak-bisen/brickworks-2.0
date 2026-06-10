package com.brickwork.finance.payment.controller.impl;

import com.brickwork.finance.payment.service.PaymentService;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/finance/webhooks")
public class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            log.info("Razorpay webhook received");
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (!isValid) {
                log.warn("Razorpay webhook rejected: invalid signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Signature");
            }

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");
            log.info("Processing Razorpay webhook event: {}", eventType);

            if ("order.paid".equals(eventType) || "payment.captured".equals(eventType)) {
                JSONObject payloadObj = event.getJSONObject("payload");
                String rzpOrderId = extractRazorpayOrderId(payloadObj, eventType);
                String rzpPaymentId = extractRazorpayPaymentId(payloadObj, eventType);

                if (rzpOrderId != null && rzpPaymentId != null) {
                    paymentService.handleWebhookPaymentCaptured(rzpOrderId, rzpPaymentId);
                    log.info("Payment captured via webhook: orderId={}, paymentId={}", rzpOrderId, rzpPaymentId);
                }
            }

            return ResponseEntity.ok("Webhook Received");

        } catch (Exception e) {
            log.error("Failed to process Razorpay webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private String extractRazorpayOrderId(JSONObject payloadObj, String eventType) {
        if ("order.paid".equals(eventType) && payloadObj.has("order")) {
            return payloadObj.getJSONObject("order").getJSONObject("entity").getString("id");
        }
        if ("payment.captured".equals(eventType) && payloadObj.has("payment")) {
            return payloadObj.getJSONObject("payment").getJSONObject("entity").getString("order_id");
        }
        return null;
    }

    private String extractRazorpayPaymentId(JSONObject payloadObj, String eventType) {
        if ("order.paid".equals(eventType) && payloadObj.has("payment")) {
            return payloadObj.getJSONObject("payment").getJSONObject("entity").getString("id");
        }
        if ("payment.captured".equals(eventType) && payloadObj.has("payment")) {
            return payloadObj.getJSONObject("payment").getJSONObject("entity").getString("id");
        }
        return null;
    }
}