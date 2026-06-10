package com.brickwork.orders.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLogFromOrderRequest {
    private String orderId;
    private List<ProductionLogFromOrderItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionLogFromOrderItem {
        private String productId;
        private Integer quantity;
    }
}