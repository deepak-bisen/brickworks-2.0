package com.brickwork.products.production.service;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import java.util.List;

public interface ProductionAnalyticsService {
    List<ProductionAnalyticsProjection> getProductionData(String timeframe);
}