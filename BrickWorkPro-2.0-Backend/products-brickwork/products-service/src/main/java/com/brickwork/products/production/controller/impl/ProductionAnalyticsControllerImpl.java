package com.brickwork.products.production.controller.impl;

import com.brickwork.products.production.controller.ProductionAnalyticsController;
import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.service.analytics.ProductionAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ProductionAnalyticsControllerImpl implements ProductionAnalyticsController {

    @Autowired
    private ProductionAnalyticsService analyticsService;

    // Example URL: GET /api/products/analytics/production?timeframe=weekly
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER','STAFF')")
    public ResponseEntity<List<ProductionAnalyticsProjection>> getProductionAnalytics(
            @RequestParam(defaultValue = "monthly") String timeframe) {
        return ResponseEntity.ok(analyticsService.getProductionData(timeframe));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        return ResponseEntity.ok(analyticsService.getTodayProductionStats());
    }
}