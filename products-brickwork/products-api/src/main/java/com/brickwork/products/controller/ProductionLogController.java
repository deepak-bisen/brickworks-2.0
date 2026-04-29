package com.brickwork.products.controller;

import com.brickwork.products.dto.ProductionLogDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/production-logs")
public interface ProductionLogController {

    @GetMapping
    ResponseEntity<List<ProductionLogDTO>> getAllLogs();

    @PostMapping
    ResponseEntity<ProductionLogDTO> createLog(@RequestBody ProductionLogDTO logDTO);
}
