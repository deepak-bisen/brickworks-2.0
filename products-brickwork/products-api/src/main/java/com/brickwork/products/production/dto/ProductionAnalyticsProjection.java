package com.brickwork.products.production.dto;

public interface ProductionAnalyticsProjection {
    String getPeriod();
    String getStage(); // MOLDING, BAKED, FINISHED
    Integer getTotalQuantity();
}