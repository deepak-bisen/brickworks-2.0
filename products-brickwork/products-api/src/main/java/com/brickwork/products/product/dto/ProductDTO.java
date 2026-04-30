package com.brickwork.products.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productId;
    private String name;
    private String color;
    private String description;
    private String brickType;
    private String category;
    private String dimensions;
    private Double unitPrice;
    private Integer stockQuantity;
    private Double estimatedCost;
    private Integer bulkDiscountThreshold;
    private String imageName;
    private String imageType;
    private byte[] imageData;

}