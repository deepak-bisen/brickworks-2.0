package com.brickwork.orders.order.service;

import com.brickwork.orders.dto.SalesAnalyticsProjection;
import com.brickwork.orders.dto.TopProductProjection;

import java.util.List;

public interface AnalyticsService {
    List<SalesAnalyticsProjection> getSalesData(String timeframe);
    List<TopProductProjection> getTopProducts(String timeframe);
}