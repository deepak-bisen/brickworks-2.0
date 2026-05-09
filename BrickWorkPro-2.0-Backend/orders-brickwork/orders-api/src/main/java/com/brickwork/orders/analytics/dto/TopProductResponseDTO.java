package com.brickwork.orders.analytics.dto;

import lombok.Data;

@Data
public class TopProductResponseDTO {
    private String period;
    private String productId;
    private String productName;
    private Long totalQuantitySold;
}
