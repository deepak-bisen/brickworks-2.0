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
    public void sendOrderConfirmationEmail(String toEmail, String customerName, String orderId, double totalAmount) {
        if (toEmail == null || toEmail.trim().isEmpty() || toEmail.equals("N/A")) return;
        log.info("Sending order confirmation email to {}", toEmail);
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
                log.info("Order confirmation email sent to {}", toEmail);
            } catch (Exception e) {
                log.error("Failed to send order confirmation email to {}", toEmail, e);
            }
        }).start();
    }

    @Override
    public void sendDispatchEmail(String toEmail, String customerName, String orderId, String driverDetails) {
        if (toEmail == null || toEmail.trim().isEmpty() || toEmail.equals("N/A")) return;

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
                log.info("Dispatch email sent to {}", toEmail);
            } catch (Exception e) {
                log.error("Failed to send dispatch email to {}", toEmail, e);
            }
        }).start();
    }
}