package com.brickwork.orders.analytics.service;

import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductResponseDTO;

import java.util.List;

public interface AnalyticsService {
    List<SalesAnalyticsProjection> getSalesData(String timeframe);
    List<TopProductResponseDTO> getTopProducts(String timeframe);
}