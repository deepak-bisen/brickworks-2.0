package com.brickwork.products.production.controller;

import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.production.dto.ProductionLogFromOrderRequest;
import com.brickwork.products.production.dto.UpdateStageRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/production-logs")
public interface ProductionLogController {

    @GetMapping
    ResponseEntity<List<ProductionLogDTO>> getAllLogs();

    @PostMapping
    ResponseEntity<ProductionLogDTO> createLog(@RequestBody ProductionLogDTO logDTO);

    @PutMapping("/{productionLogId}/stage")
    ResponseEntity<ProductionLogDTO> updateStage(@PathVariable String productionLogId, @RequestBody UpdateStageRequestDTO request);

    @PostMapping("/from-order")
    ResponseEntity<List<ProductionLogDTO>> createFromOrder(@RequestBody ProductionLogFromOrderRequest request);
}
