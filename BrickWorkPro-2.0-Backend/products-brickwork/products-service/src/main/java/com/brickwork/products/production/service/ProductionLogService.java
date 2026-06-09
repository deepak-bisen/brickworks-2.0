package com.brickwork.products.production.service;

import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.production.enums.ProductionStage;

import java.util.List;

public interface ProductionLogService {
    ProductionLogDTO createLog(ProductionLogDTO dto);
    List<ProductionLogDTO> getAllLogs();
    ProductionLogDTO updateStage(String productionLogId, ProductionStage newStage);
}