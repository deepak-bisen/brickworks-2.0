package com.brickwork.products.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "raw_materials")
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String rawMaterialId;
    private String name; // e.g., "Soil", "Coal", "Sand"
    private String unitOfMeasure; // e.g., "Tons", "Kg"
    private Double currentStockLevel;
}
