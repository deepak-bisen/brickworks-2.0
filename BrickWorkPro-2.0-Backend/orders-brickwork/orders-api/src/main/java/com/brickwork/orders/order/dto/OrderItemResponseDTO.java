package com.brickwork.orders.order.dto;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private String productId;
    private String productName;
    private int quantity;
    private Double unitPrice;
}