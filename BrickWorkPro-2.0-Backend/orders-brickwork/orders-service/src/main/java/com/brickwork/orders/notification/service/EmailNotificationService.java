package com.brickwork.orders.notification.service;

public interface EmailNotificationService {
    boolean sendOrderConfirmationEmail(String toEmail, String customerName, String orderId, double totalAmount);
    boolean sendDispatchEmail(String toEmail, String customerName, String orderId, String driverDetails);
}