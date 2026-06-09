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
        private final String INSTANCE_ID = "instance178831";
        private final String TOKEN = "t8s1ujkdry48jh8a";
        private final String API_URL = "https://api.ultramsg.com/" + INSTANCE_ID + "/messages/chat";

    @Override
    public void sendDispatchNotification(String customerPhone, String orderId, String driverDetails) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                // Format the message
                String message = "🚚 *BrickWorks Pro Update* \n\n" +
                        "Your Order #" + orderId + " has been DISPATCHED!\n" +
                        "Driver Details: " + driverDetails + "\n\n" +
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
                        System.out.println("WhatsApp Webhook Fired for order Confirmation: " + response.getStatusCode());
                    } catch (Exception e) {
                        System.err.println("Failed to send WhatsApp message for order Confirmation: " + e.getMessage());
                    }
                }).start();

            } catch (Exception e) {
                System.err.println("Error formatting WhatsApp request: " + e.getMessage());
            }
        }

        /** open api method */
    @Override
    public void sendOrderConfirmation(String customerPhone, String orderId, double totalAmount) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Format the Confirmation message
            String message = "🧱 *BrickWorks Pro - Order Confirmed!* \n\n" +
                    "Thank you for your business! Your Order #" + orderId + " has been successfully received.\n" +
                    "Total Amount: ₹" + totalAmount + "\n\n" +
                    "We will notify you again once your order has been dispatched.";

            // Prepare API Request Body
            Map<String, String> body = new HashMap<>();
            body.put("token", TOKEN);
            body.put("to", customerPhone);
            body.put("body", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            // Fire asynchronously
            new Thread(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
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

    /**
     * package com.brickwork.orders.notification.service.impl;
     *
     * import com.brickwork.orders.notification.service.WhatsAppNotificationService;
     * import org.springframework.stereotype.Service;
     * import org.springframework.web.client.RestTemplate;
     *
     * import java.net.URLEncoder;
     * import java.nio.charset.StandardCharsets;
     *
     * @Service
     * public class WhatsAppNotificationServiceImpl implements WhatsAppNotificationService {
     *
     *     // IMPORTANT: Replace this with the key you get from the CallMeBot WhatsApp chat
     *     private final String API_KEY = "YOUR_CALLMEBOT_API_KEY";
     *
     *     @Override
     *     public void sendDispatchNotification(String customerPhone, String orderId, String driverDetails) {
     *         try {
     *             String message = "🚚 *BrickWorks Pro Update* \n\n" +
     *                     "Your Order #" + orderId + " has been DISPATCHED!\n" +
     *                     "Driver Details: " + driverDetails + "\n\n" +
     *                     "Track your order via the Customer Portal.";
     *
     *             sendViaCallMeBot(customerPhone, message);
     *         } catch (Exception e) {
     *             System.err.println("Error formatting dispatch message: " + e.getMessage());
     *         }
     *     }
     *
     *     @Override
     *     public void sendOrderConfirmation(String customerPhone, String orderId, double totalAmount) {
     *         try {
     *             String message = "🧱 *BrickWorks Pro - Order Confirmed!* \n\n" +
     *                     "Thank you! Your Order #" + orderId + " has been successfully received.\n" +
     *                     "Total Amount: ₹" + totalAmount + "\n\n" +
     *                     "We will notify you again once your order has been dispatched.";
     *
     *             sendViaCallMeBot(customerPhone, message);
     *         } catch (Exception e) {
     *             System.err.println("Error formatting confirmation message: " + e.getMessage());
     *         }
     *     }
     *
     *     /// --- Core Helper Method for CallMeBot ---
     *     private void sendViaCallMeBot(String phone, String message) {
     *         // Fire asynchronously so we don't block the Spring Boot thread
     *         new Thread(() -> {
     *             try {
     *                 RestTemplate restTemplate = new RestTemplate();
     *
     *                 // 🚀 FIX: Message ke sath-sath Phone number ko bhi encode karna zaroori hai!
     *                 String encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8.toString());
     *                 String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
     *
     *                 // Construct the CallMeBot URL
     *                 String url = "https://api.callmebot.com/whatsapp.php?phone=" + encodedPhone +
     *                         "&text=" + encodedMessage + "&apikey=" + API_KEY;
     *
     *                 // Send the GET request
     *                 restTemplate.getForEntity(url, String.class);
     *                 System.out.println("✅ WhatsApp Notification sent to " + phone);
     *
     *             } catch (Exception e) {
     *                 System.err.println("❌ Failed to send WhatsApp message: " + e.getMessage());
     *             }
     *         }).start();
     *     }
     * }
     */