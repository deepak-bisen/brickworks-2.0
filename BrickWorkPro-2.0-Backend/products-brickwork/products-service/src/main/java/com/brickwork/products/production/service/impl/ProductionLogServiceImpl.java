package com.brickwork.products.production.service.impl;

import com.brickwork.products.product.entity.Product;
import com.brickwork.products.production.dto.ProductionLogDTO;
import com.brickwork.products.production.dto.ProductionLogFromOrderRequest;
import com.brickwork.products.production.entity.ProductionLog;
import com.brickwork.products.product.repository.ProductRepository;
import com.brickwork.products.production.enums.ProductionStage;
import com.brickwork.products.production.repository.ProductionLogRepository;
import com.brickwork.products.production.service.ProductionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.brickwork.exception.NotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        ProductionLog productionLog = new ProductionLog();
        productionLog.setManagerId(productionLogDTO.getManagerId());
        productionLog.setStage(productionLogDTO.getStage()!= null ? productionLogDTO.getStage() : ProductionStage.MOLDED);
        productionLog.setQuantity(productionLogDTO.getQuantity());
        productionLog.setCreatedAt(productionLogDTO.getCreatedAt() != null ? productionLogDTO.getCreatedAt() : java.time.LocalDateTime.now());

        if (productionLogDTO.getProductId() != null) {
            Product product = productRepository.findById(productionLogDTO.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
            productionLog.setProduct(product);

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
        productionLog.setManagerName(managerName != null ? managerName :
                (productionLogDTO.getManagerName() != null ? productionLogDTO.getManagerName() : productionLogDTO.getManagerId()));
        ProductionLog savedLog = logRepository.save(productionLog);
        log.info("Created production log: id={}, productId={}, quantity={}, stage={}",
                savedLog.getId(), productionLogDTO.getProductId(), savedLog.getQuantity(), savedLog.getStage());
        // NOTE: Raw material inventory is managed independently via the
        // /api/raw-materials endpoints (add/update). Production logs are
        // intentionally decoupled from automatic raw-material deduction so
        // staff can manage material consumption manually as needed.

        return mapToDTO(savedLog);
    }


    @Override
    @Transactional
    public List<ProductionLogDTO> createFromOrder(ProductionLogFromOrderRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one order item is required");
        }

        List<ProductionLogDTO> createdLogs = new ArrayList<>();
        for (ProductionLogFromOrderRequest.ProductionLogFromOrderItem item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each item must have a productId and positive quantity");
            }

            ProductionLog productionLog = new ProductionLog();
            productionLog.setManagerId("internal-service");
            productionLog.setManagerName("Order System");
            productionLog.setStage(ProductionStage.MOLDED);
            productionLog.setQuantity(item.getQuantity());
            productionLog.setCreatedAt(LocalDateTime.now());
            productionLog.setOrderId(request.getOrderId());

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + item.getProductId()));
            productionLog.setProduct(product);

            createdLogs.add(mapToDTO(logRepository.save(productionLog)));
        }

        log.info("Created {} production logs from order: orderId={}", createdLogs.size(), request.getOrderId());
        return createdLogs;
    }

    @Override
    public List<ProductionLogDTO> getAllLogs() {
        return logRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductionLogDTO updateStage(String productionLogId, ProductionStage newStage) {
        ProductionLog productionLog = logRepository.findById(productionLogId)
                .orElseThrow(() -> new NotFoundException("Production log not found"));

        ProductionStage currentStage = productionLog.getStage();

        boolean validTransition = (currentStage == ProductionStage.MOLDED && newStage == ProductionStage.IN_KILN) || (currentStage == ProductionStage.IN_KILN && newStage == ProductionStage.BAKED);

        if (!validTransition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid production stage transition");
        }

        productionLog.setStage(newStage);

    //  INVENTORY UPDATE ONLY WHEN PRODUCT IS FULLY BAKED
        if (newStage == ProductionStage.BAKED) {

            Product product = productionLog.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + productionLog.getQuantity());
                productRepository.save(product);
                log.info("Updated inventory after baking: productId={}, quantityAdded={}",
                        product.getProductId(), productionLog.getQuantity());
            }
        }

        log.info("Updated production log stage: id={}, {} -> {}", productionLogId, currentStage, newStage);
        return mapToDTO(logRepository.save(productionLog));
    }


    private ProductionLogDTO mapToDTO(ProductionLog productionLog) {
        return new ProductionLogDTO(
                productionLog.getId(),
                productionLog.getManagerId(),
                productionLog.getManagerName(),
                productionLog.getProduct() != null ? productionLog.getProduct().getProductId() : null,
                productionLog.getProduct() != null ? productionLog.getProduct().getName() : null,
                productionLog.getStage(),
                productionLog.getQuantity(),
                productionLog.getCreatedAt(),
                productionLog.getOrderId());
    }
}