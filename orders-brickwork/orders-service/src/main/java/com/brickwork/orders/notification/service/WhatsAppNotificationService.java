package com.brickwork.orders.notification.service;

public interface WhatsAppNotificationService {

    void sendDispatchNotification(String customerPhone, String orderId);
}
