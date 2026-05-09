package com.brickwork.orders.analytics.dto;

public interface SalesAnalyticsProjection {
    String getPeriod();       // Will hold the formatted date (e.g., "2026", "2026-05", "2026-W18")
    Double getTotalRevenue(); // Sum of totalAmount
    Double getTotalProfit();  // Sum of totalProfit
}