package com.brickwork.products.production.controller;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/products/analytics")
public interface ProductionAnalyticsController {

    @GetMapping("/production")
    ResponseEntity<List<ProductionAnalyticsProjection>> getProductionAnalytics(
            @RequestParam(defaultValue = "monthly") String timeframe);


    @GetMapping("/stats/daily")
    ResponseEntity<Map<String, Object>> getDailyStats();
}
