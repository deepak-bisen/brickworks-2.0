package com.brickwork.orders.notification.service.impl;

import com.brickwork.orders.notification.service.WhatsAppNotificationService;
import org.springframework.beans.factory.annotation.Value;
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
                    System.out.println("WhatsApp Webhook Fired for order Confirmation: " + response.getStatusCode());
                } catch (Exception e) {
                    System.err.println("Failed to send WhatsApp message for order Confirmation: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error formatting WhatsApp request: " + e.getMessage());
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
                    System.out.println("WhatsApp Order Dispatch Confirmation Fired: " + response.getStatusCode());
                } catch (Exception e) {
                    System.err.println("Failed to send Order Dispatch WhatsApp confirmation: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error formatting WhatsApp request: " + e.getMessage());
        }
    }
}