package com.brickwork.orders.notification.service.impl;

import com.brickwork.orders.notification.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public boolean sendOrderConfirmationEmail(String toEmail, String customerName, String orderId, double totalAmount) {
        if (toEmail == null || toEmail.trim().isEmpty() || toEmail.equals("N/A")) return false;
        log.info("Order {}: Sending CONFIRMATION EMAIL to recipient {} (success will be logged after send)", orderId, toEmail);
        final boolean[] result = {false};
        new Thread(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Order Confirmed - BrickWorks Pro (#" + orderId + ")");
                message.setText("Dear " + customerName + ",\n\n" +
                        "Thank you for choosing BrickWorks Pro! Your order has been successfully placed.\n\n" +
                        "Order ID: " + orderId + "\n" +
                        "Total Amount: ₹" + totalAmount + "\n\n" +
                        "You can track your order live on our website.\n\n" +
                        "Regards,\nBrickWorks Pro Team");

                mailSender.send(message);
                log.info("Order {}: CONFIRMATION EMAIL sent successfully to recipient {}", orderId, toEmail);
                result[0] = true;
            } catch (Exception e) {
                log.info("Order {}: CONFIRMATION EMAIL FAILED for recipient {} - error: {}", orderId, toEmail, e.getMessage());
                log.error("Order {}: Failed to send CONFIRMATION EMAIL to {}", orderId, toEmail, e);
            }
        }).start();
        // Note: async, return optimistic true for caller to record attempt; detailed success in async log
        return true;
    }

    @Override
    public boolean sendDispatchEmail(String toEmail, String customerName, String orderId, String driverDetails) {
        if (toEmail == null || toEmail.trim().isEmpty() || toEmail.equals("N/A")) return false;
        log.info("Order {}: Sending DISPATCH EMAIL to recipient {} (driver: {})", orderId, toEmail, driverDetails);
        new Thread(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Order Dispatched! - BrickWorks Pro (#" + orderId + ")");
                message.setText("Dear " + customerName + ",\n\n" +
                        "Great news! Your order #" + orderId + " has been dispatched from our factory.\n\n" +
                        "Driver Details: " + driverDetails + "\n\n" +
                        "Track your order on our portal. Thank you for your business!\n\n" +
                        "Regards,\nBrickWorks Pro Team");

                mailSender.send(message);
                log.info("Order {}: DISPATCH EMAIL sent successfully to recipient {}", orderId, toEmail);
            } catch (Exception e) {
                log.info("Order {}: DISPATCH EMAIL FAILED for recipient {} - error: {}", orderId, toEmail, e.getMessage());
                log.error("Order {}: Failed to send DISPATCH EMAIL to {}", orderId, toEmail, e);
            }
        }).start();
        return true;
    }
}