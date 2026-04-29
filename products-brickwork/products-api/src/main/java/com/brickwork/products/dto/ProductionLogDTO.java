package com.brickwork.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogDTO {
    private String id;
    private String managerId;
    private String productId; // Using String ID for the product reference
    private String stage;
    private Integer quantity;
    private LocalDate logDate;
}