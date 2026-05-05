package com.brickwork.orders.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productId;
    private String name;
    private Double unitPrice; // Selling Price
    private Double estimatedCost; // Needed for Profit calc
    private Integer bulkDiscountThreshold; // Needed for Discount calc
}