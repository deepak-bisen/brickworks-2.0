package com.brickwork.orders.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderStatus {
    QUOTE_REQUEST,      // 0. Public lead, no financial impact yet
    PENDING_PAYMENT,    // 1. Formal order placed, waiting for contractor payment
    PAYMENT_RECEIVED,   // 2. Payment confirmed (Finance Service will trigger this)
    IN_PRODUCTION,      // 3. Sent to the factory queue
    DISPATCHED,         // 4. On the truck
    DELIVERED,          // 5. Finalized
    CANCELLED,          // 6. Voided order/quote
    CONFIRMED_COD;       // 7. For confirmed VIA COD

    @JsonCreator
    public static OrderStatus fromString(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
}