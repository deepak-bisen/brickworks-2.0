package com.brickwork.orders.notification.service.impl;

import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppNotificationServiceImpl implements WhatsAppNotificationService {

    @Value("${whatsapp.instance.id:change-me}")
    private String instanceId;

    @Value("${whatsapp.token:change-me}")
    private String token;

    private String getApiUrl() {
        return "https://api.ultramsg.com/" + instanceId + "/messages/chat";
    }

    @Override
    public void sendDispatchNotification(String customerPhone, String orderId, String driverDetails) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String message = "🚚 *BrickWorks Pro Update* \n\n" +
                    "Your Order #" + orderId + " has been DISPATCHED!\n" +
                    "Driver Details: " + driverDetails + "\n\n" +
                    "Track your order and download your invoice via the Customer Portal. Thank you for choosing BrickWorks!";

            Map<String, String> body = new HashMap<>();
            body.put("token", token);
            body.put("to", customerPhone);
            body.put("body", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            new Thread(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), request, String.class);
                    log.info("WhatsApp dispatch notification sent for order {}, status={}", orderId, response.getStatusCode());
                } catch (Exception e) {
                    log.error("Failed to send WhatsApp dispatch notification for order {}", orderId, e);
                }
            }).start();

        } catch (Exception e) {
            log.error("Error preparing WhatsApp dispatch notification for order {}", orderId, e);
        }
    }

    @Override
    public void sendOrderConfirmation(String customerPhone, String orderId, double totalAmount) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String message = "🧱 *BrickWorks Pro - Order Confirmed!* \n\n" +
                    "Thank you for your business! Your Order #" + orderId + " has been successfully received.\n" +
                    "Total Amount: ₹" + totalAmount + "\n\n" +
                    "We will notify you again once your order has been dispatched.";

            Map<String, String> body = new HashMap<>();
            body.put("token", token);
            body.put("to", customerPhone);
            body.put("body", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            new Thread(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), request, String.class);
                    log.info("WhatsApp order confirmation sent for order {}, status={}", orderId, response.getStatusCode());
                } catch (Exception e) {
                    log.error("Failed to send WhatsApp order confirmation for order {}", orderId, e);
                }
            }).start();

        } catch (Exception e) {
            log.error("Error preparing WhatsApp order confirmation for order {}", orderId, e);
        }
    }
}