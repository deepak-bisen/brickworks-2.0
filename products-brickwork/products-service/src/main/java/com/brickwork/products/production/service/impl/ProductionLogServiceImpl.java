package com.brickwork.products.production.service.impl;

import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.product.entity.Product;
import com.brickwork.products.production.entity.ProductionLog;
import com.brickwork.products.product.repository.ProductRepository;
import com.brickwork.products.production.repository.ProductionLogRepository;
import com.brickwork.products.production.service.ProductionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionLogServiceImpl implements ProductionLogService {

    @Autowired
    private ProductionLogRepository logRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional // Ensures both the log and the product update succeed together
    public ProductionLogDTO createLog(ProductionLogDTO dto) {
        ProductionLog log = new ProductionLog();
        log.setManagerId(dto.getManagerId());
        log.setStage(dto.getStage());
        log.setQuantity(dto.getQuantity());
        log.setLogDate(dto.getLogDate());

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            log.setProduct(product);

            // NEW: Automatically update inventory when production finishes!
            if ("BAKED".equalsIgnoreCase(dto.getStage()) || "FINISHED".equalsIgnoreCase(dto.getStage())) {
                int currentStock = product.getStockQuantity() != 0 ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock + dto.getQuantity());
                productRepository.save(product);
            }
        }

        ProductionLog savedLog = logRepository.save(log);
        return mapToDTO(savedLog);
    }

    @Override
    public List<ProductionLogDTO> getAllLogs() {
        return logRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ProductionLogDTO mapToDTO(ProductionLog log) {
        return new ProductionLogDTO(
                log.getProductionLogId(),
                log.getManagerId(),
                log.getProduct() != null ? log.getProduct().getProductId() : null,
                log.getStage(),
                log.getQuantity(),
                log.getLogDate()
        );
    }
}