package com.brickwork.products.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PRODUCTS")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String productId;

    // --- PHASE 1: Existing Catalog Fields ---
    @Column(nullable = false)
    private String name;

    @Column
    private String color;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String brickType;

    @Column(nullable = false)
    private String dimensions;


    @Column(nullable = false)
    private Double unitPrice;   //selling price

    @Column(nullable = false)
    private int stockQuantity;     // Current Inventory

    @Column
    private String imageName;

    @Column
    private String imageType;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;


    // --- PHASE 2: New BI & Enterprise Fields ---
    @Column(nullable = false)
    private Double estimatedCost; // For Profit/Loss Calculation

    @Column(nullable = false)
    private Integer bulkDiscountThreshold; // Quantity required to trigger automatic discount

}
