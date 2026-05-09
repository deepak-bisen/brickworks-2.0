package com.brickwork.products.production.service.impl;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.repository.ProductionLogRepository;
import com.brickwork.products.production.service.ProductionAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}