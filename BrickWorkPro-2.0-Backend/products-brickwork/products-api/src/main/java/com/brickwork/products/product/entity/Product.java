package com.brickwork.products.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "PRODUCTS")
@Data
public class Product {

    @Id
    @Column(name = "PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String productId;

    // --- PHASE 1: Existing Catalog Fields ---
    @Column(name = "NAME" , columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "BRICK_COLOR", columnDefinition = "VARCHAR(20)",  nullable = true)
    private String color;

    @Column(name = "DESCRIPTION",columnDefinition = "VARCHAR(255)", nullable = false)
    private String description;

    @Column(name = "CATEGORY", columnDefinition = "VARCHAR(50)",nullable = false)
    private String category;

    @Column(name = "BRICK_TYPE", columnDefinition = "VARCHAR(50)", nullable = false)
    private String brickType;

    @Column(name = "DIMENSIONS", columnDefinition = "VARCHAR(25)", nullable = false)
    private String dimensions;


    @Column(name = "UNIT_PRICE", columnDefinition = "VARCHAR(10)", nullable = false)
    private Double unitPrice;   //selling price

    @Column(name = "STOCK_QUANTITY", columnDefinition = "INT", nullable = false)
    private int stockQuantity;     // Current Inventory

    // --- PHASE 2: New BI & Enterprise Fields ---
    @Column(name = "ESTIMATED_COST", columnDefinition = "DOUBLE", nullable = false)
    private Double estimatedCost; // For Profit/Loss Calculation

    @Column(name = "BULK_DISCOUNT_THRESHOLD", columnDefinition = "INT", nullable = false)
    private Integer bulkDiscountThreshold; // Quantity required to trigger automatic discount

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttachment> attachments;

}
