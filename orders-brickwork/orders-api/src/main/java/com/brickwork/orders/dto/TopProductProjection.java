package com.brickwork.orders.dto;

public interface TopProductProjection {
    String getPeriod();        // The Year, Month, or Week
    Long getProductId();       // ID of the product
    String getProductName();   // Name of the product (Needs to be fetched from Products Service via Feign, OR we just return the ID if the frontend handles the lookup. For simplicity, let's just return ID and Quantity sold).
    Long getTotalQuantitySold(); // Total bricks sold
}