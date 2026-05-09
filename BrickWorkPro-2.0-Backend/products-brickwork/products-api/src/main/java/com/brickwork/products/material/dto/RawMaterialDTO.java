package com.brickwork.products.material.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialDTO {
    private String rawMaterialId;
    private String name;
    private String unitOfMeasure;
    private Double currentStockLevel;
}