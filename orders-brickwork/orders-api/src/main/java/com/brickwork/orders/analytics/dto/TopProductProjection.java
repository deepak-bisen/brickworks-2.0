package com.brickwork.orders.analytics.dto;

public interface TopProductProjection {
    String getPeriod();        // The Year, Month, or Week
    String getProductId();       // ID of the product
    Long getTotalQuantitySold(); // Total bricks sold
    //   String getProductName();   // Name of the product (Needs to be fetched from Products Service via Feign, OR we just return the ID if the frontend handles the lookup. For simplicity, let's just return ID and Quantity sold).

}