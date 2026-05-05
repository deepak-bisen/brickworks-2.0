package com.brickwork.orders.analytics.controller.impl;

import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductResponseDTO;
import com.brickwork.orders.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/analytics")
public class AnalyticsControllerImpl {

    @Autowired
    private AnalyticsService analyticsService;

    // Example URL: GET /api/orders/analytics/sales?timeframe=monthly
    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesAnalyticsProjection>> getSalesAnalytics(
            @RequestParam(defaultValue = "monthly") String timeframe) {
        return ResponseEntity.ok(analyticsService.getSalesData(timeframe));
    }


    // Example URL: GET /api/orders/analytics/top-products?timeframe=monthly
    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopProductResponseDTO>> getTopProducts(
            @RequestParam(defaultValue = "monthly") String timeframe) {

        List<TopProductResponseDTO> topProducts = analyticsService.getTopProducts(timeframe);
        return ResponseEntity.ok(topProducts);
    }
}