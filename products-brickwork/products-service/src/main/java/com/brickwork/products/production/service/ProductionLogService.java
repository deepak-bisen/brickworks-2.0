package com.brickwork.products.production.service;

import com.brickwork.products.production.dto.ProductionLogDTO;
import java.util.List;

public interface ProductionLogService {
    ProductionLogDTO createLog(ProductionLogDTO dto);
    List<ProductionLogDTO> getAllLogs();
}