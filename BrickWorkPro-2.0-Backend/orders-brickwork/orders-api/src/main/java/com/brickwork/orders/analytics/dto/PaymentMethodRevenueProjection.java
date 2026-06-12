package com.brickwork.orders.analytics.dto;

public interface PaymentMethodRevenueProjection {
    String getPaymentMethod();
    Double getTotalRevenue();
    Long getOrderCount();
}
