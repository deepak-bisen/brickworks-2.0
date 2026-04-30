package com.brickwork.products.production.controller.impl;

import com.brickwork.products.production.controller.ProductionLogController;
import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.production.service.ProductionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ProductionLogControllerImpl implements ProductionLogController {

    @Autowired
    private ProductionLogService service;

    @Override
    public ResponseEntity<List<ProductionLogDTO>> getAllLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }

    @Override
    public ResponseEntity<ProductionLogDTO> createLog(ProductionLogDTO logDTO) {
        return ResponseEntity.ok(service.createLog(logDTO));
    }
}
