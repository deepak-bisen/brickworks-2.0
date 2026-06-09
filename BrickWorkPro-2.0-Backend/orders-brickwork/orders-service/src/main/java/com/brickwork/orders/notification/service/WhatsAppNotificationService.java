package com.brickwork.orders.notification.service;

public interface WhatsAppNotificationService {

    void sendDispatchNotification(String customerPhone, String orderId, String driverDetails);
    void sendOrderConfirmation(String customerPhone, String orderId, double totalAmount);
}
