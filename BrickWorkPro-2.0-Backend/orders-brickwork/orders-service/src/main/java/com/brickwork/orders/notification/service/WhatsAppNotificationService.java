package com.brickwork.orders.notification.service;

public interface WhatsAppNotificationService {

    boolean sendDispatchNotification(String customerPhone, String orderId, String driverDetails);
    boolean sendOrderConfirmation(String customerPhone, String orderId, double totalAmount);
}
