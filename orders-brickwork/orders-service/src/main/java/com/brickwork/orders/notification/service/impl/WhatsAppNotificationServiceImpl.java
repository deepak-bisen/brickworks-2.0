package com.brickwork.orders.notification.service.impl;

import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppNotificationServiceImpl implements WhatsAppNotificationService {


        // Using UltraMsg (a popular, easy API for WhatsApp in student projects)
        // You can replace these with actual API keys later
        private final String INSTANCE_ID = "YOUR_INSTANCE_ID";
        private final String TOKEN = "YOUR_ULTRAMSG_TOKEN";
        private final String API_URL = "https://api.ultramsg.com/" + INSTANCE_ID + "/messages/chat";

    @Override
    public void sendDispatchNotification(String customerPhone, String orderId) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                // Format the message
                String message = "🚚 *BrickWorks Pro Update* \n\n" +
                        "Your Order #" + orderId + " has been DISPATCHED!\n" +
                        "Track your order and download your invoice via the Customer Portal. Thank you for choosing BrickWorks!";

                // Prepare API Request Body
                Map<String, String> body = new HashMap<>();
                body.put("token", TOKEN);
                body.put("to", customerPhone); // e.g., "+919876543210"
                body.put("body", message);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

                // Fire the Webhook asynchronously (don't block the main thread)
                new Thread(() -> {
                    try {
                        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
                        System.out.println("WhatsApp Webhook Fired: " + response.getStatusCode());
                    } catch (Exception e) {
                        System.err.println("Failed to send WhatsApp message: " + e.getMessage());
                    }
                }).start();

            } catch (Exception e) {
                System.err.println("Error formatting WhatsApp request: " + e.getMessage());
            }
        }
    }