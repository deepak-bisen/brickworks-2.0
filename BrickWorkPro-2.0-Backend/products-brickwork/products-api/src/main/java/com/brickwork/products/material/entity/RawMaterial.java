package com.brickwork.products.material.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "raw_materials")
public class RawMaterial {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "MATERIAL_NAME", columnDefinition = "VARCHAR(25)", nullable = false)
    private String materialName; // e.g., "Soil", "Coal", "Sand"

    @Column(name = "UNIT_OF_MEASURE", columnDefinition = "VARCHAR(25)", nullable = false)
    private String unitOfMeasure; // e.g., "Tons", "Kg"

    @Column(name = "CURRENT_STOCK_LEVEL", columnDefinition = "DOUBLE", nullable = false)
    private Double currentStockLevel;
}
