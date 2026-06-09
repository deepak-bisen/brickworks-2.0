package com.brickwork.products.production.service.analytics.impl;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.repository.ProductionLogRepository;
import com.brickwork.products.production.service.analytics.ProductionAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProductionAnalyticsServiceImpl implements ProductionAnalyticsService {

    @Autowired
    private ProductionLogRepository logRepository;

    @Override
    public List<ProductionAnalyticsProjection> getProductionData(String timeframe) {
        return switch (timeframe.toLowerCase()) {
            case "yearly" -> logRepository.getYearlyProductionAnalytics();
            case "monthly" -> logRepository.getMonthlyProductionAnalytics();
            case "weekly" -> logRepository.getWeeklyProductionAnalytics();
            default -> throw new IllegalArgumentException("Invalid timeframe.");
        };
    }

    @Override
    public Map<String, Object> getTodayProductionStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        Long totalProduced = logRepository.sumProductionByDate(startOfDay, endOfDay);

        // Default to 0 if no production yet
        Map<String, Object> stats = new HashMap<>();
        stats.put("dailyProduction", totalProduced != null ? totalProduced : 0);
        stats.put("lastUpdated", LocalDateTime.now());

        return stats;
    }


}
