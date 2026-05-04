package com.brickwork.products.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogDTO {
    private String id;
    private String managerId;
    private String productId; // Using String ID for the product reference
    private String stage;
    private Integer quantity;
    private LocalDateTime createdAt;
}