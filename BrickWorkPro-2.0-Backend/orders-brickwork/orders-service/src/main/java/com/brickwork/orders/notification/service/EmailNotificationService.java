package com.brickwork.orders.notification.service;

public interface EmailNotificationService {
    void sendOrderConfirmationEmail(String toEmail, String customerName, String orderId, double totalAmount);
    void sendDispatchEmail(String toEmail, String customerName, String orderId, String driverDetails);
}