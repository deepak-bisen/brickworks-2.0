package com.brickwork.products.production.controller.impl;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.service.ProductionAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/analytics")
public class ProductionAnalyticsControllerImpl {

    @Autowired
    private ProductionAnalyticsService analyticsService;

    // Example URL: GET /api/products/analytics/production?timeframe=weekly
    @GetMapping("/production")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductionAnalyticsProjection>> getProductionAnalytics(
            @RequestParam(defaultValue = "monthly") String timeframe) {
        return ResponseEntity.ok(analyticsService.getProductionData(timeframe));
    }
}