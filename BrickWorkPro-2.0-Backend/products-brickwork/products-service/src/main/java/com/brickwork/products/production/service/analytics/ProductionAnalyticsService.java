package com.brickwork.products.production.service.analytics;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.repository.ProductionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProductionAnalyticsService {
    List<ProductionAnalyticsProjection> getProductionData(String timeframe);
    Map<String, Object> getTodayProductionStats();
}