package com.brickwork.products.production.dto;

import com.brickwork.products.production.enums.ProductionStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogDTO {
    private String id;
    private String managerId;
    private String managerName;
    private String productId; // Using String ID for the product reference
    private String productName;
    private ProductionStage stage;
    private Integer quantity;
    private LocalDateTime createdAt;
    private String orderId;

}