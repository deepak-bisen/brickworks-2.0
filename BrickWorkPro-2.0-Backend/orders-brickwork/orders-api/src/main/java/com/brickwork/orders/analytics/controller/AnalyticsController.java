package com.brickwork.orders.analytics.controller;

import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface AnalyticsController {

    // Example URL: GET /api/orders/analytics/sales?timeframe=monthly
    ResponseEntity<List<SalesAnalyticsProjection>> getSalesAnalytics(
            @RequestParam(defaultValue = "monthly") String timeframe);


    // Example URL: GET /api/orders/analytics/top-products?timeframe=monthly
    ResponseEntity<List<TopProductResponseDTO>> getTopProducts(
            @RequestParam(defaultValue = "monthly") String timeframe);

}