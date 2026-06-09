package com.brickwork.products.production.service.impl;

import com.brickwork.products.product.entity.Product;
import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.production.entity.ProductionLog;
import com.brickwork.products.product.repository.ProductRepository;
import com.brickwork.products.production.enums.ProductionStage;
import com.brickwork.products.production.repository.ProductionLogRepository;
import com.brickwork.products.production.service.ProductionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionLogServiceImpl implements ProductionLogService {

    private final ProductionLogRepository logRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductionLogServiceImpl(ProductionLogRepository logRepository, ProductRepository productRepository) {
        this.logRepository = logRepository;
        this.productRepository = productRepository;
    }
    // Raw material consumption is now managed independently; removed automatic deduction

    @Override
    @Transactional // Ensures both the log and the product update succeed together
    public ProductionLogDTO createLog(ProductionLogDTO productionLogDTO) {
        if (productionLogDTO.getQuantity() == null || productionLogDTO.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        // 1. save the logs
        ProductionLog log = new ProductionLog();
        log.setManagerId(productionLogDTO.getManagerId());
        log.setStage(productionLogDTO.getStage()!= null ? productionLogDTO.getStage() : ProductionStage.MOLDED);
        log.setQuantity(productionLogDTO.getQuantity());
        log.setCreatedAt(productionLogDTO.getCreatedAt() != null ? productionLogDTO.getCreatedAt() : java.time.LocalDateTime.now());

        if (productionLogDTO.getProductId() != null) {
            Product product = productRepository.findById(productionLogDTO.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
            log.setProduct(product);

        /*
         IMPORTANT CHANGE:
         DO NOT update inventory here anymore.
         Inventory should ONLY update when: IN_KILN -> BAKED
         Otherwise molded products incorrectly increase stock.
        */
        }

        String managerName = null;
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                managerName = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {
        }
        log.setManagerName(managerName != null ? managerName :
                (productionLogDTO.getManagerName() != null ? productionLogDTO.getManagerName() : productionLogDTO.getManagerId()));
        ProductionLog savedLog = logRepository.save(log);
        // NOTE: Raw material inventory is managed independently via the
        // /api/raw-materials endpoints (add/update). Production logs are
        // intentionally decoupled from automatic raw-material deduction so
        // staff can manage material consumption manually as needed.

        return mapToDTO(savedLog);
    }


    @Override
    public List<ProductionLogDTO> getAllLogs() {
        return logRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductionLogDTO updateStage(String productionLogId, ProductionStage newStage) {
        ProductionLog log = logRepository.findById(productionLogId)
                .orElseThrow(() -> new RuntimeException("Production log not found"));

        ProductionStage currentStage = log.getStage();

        boolean validTransition = (currentStage == ProductionStage.MOLDED && newStage == ProductionStage.IN_KILN) || (currentStage == ProductionStage.IN_KILN && newStage == ProductionStage.BAKED);

        if (!validTransition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid production stage transition");
        }

        log.setStage(newStage);

    //  INVENTORY UPDATE ONLY WHEN PRODUCT IS FULLY BAKED
        if (newStage == ProductionStage.BAKED) {

            Product product = log.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + log.getQuantity());
                productRepository.save(product);
            }
        }

        return mapToDTO(logRepository.save(log));
    }


    private ProductionLogDTO mapToDTO(ProductionLog log) {
        return new ProductionLogDTO(
                log.getId(),
                log.getManagerId(),
                log.getManagerName(),
                log.getProduct() != null ? log.getProduct().getProductId() : null,
                log.getProduct() != null ? log.getProduct().getName() : null,
                log.getStage(),
                log.getQuantity(),
                log.getCreatedAt());
    }
}