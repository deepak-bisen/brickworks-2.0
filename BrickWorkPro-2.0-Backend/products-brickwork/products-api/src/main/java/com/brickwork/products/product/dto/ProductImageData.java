package com.brickwork.products.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductImageData {
    private byte[] data;
    private String contentType;
}