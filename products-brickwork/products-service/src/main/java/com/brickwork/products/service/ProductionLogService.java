package com.brickwork.products.service;

import com.brickwork.products.dto.ProductionLogDTO;
import java.util.List;

public interface ProductionLogService {
    ProductionLogDTO createLog(ProductionLogDTO dto);
    List<ProductionLogDTO> getAllLogs();
}