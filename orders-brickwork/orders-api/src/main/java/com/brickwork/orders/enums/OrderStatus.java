package com.brickwork.orders.enums;

public enum OrderStatus {
    QUOTE_REQUEST,      // 0. Public lead, no financial impact yet
    PENDING_PAYMENT,    // 1. Formal order placed, waiting for contractor payment
    PAYMENT_RECEIVED,   // 2. Payment confirmed (Finance Service will trigger this)
    IN_PRODUCTION,      // 3. Sent to the factory queue
    DISPATCHED,         // 4. On the truck
    DELIVERED,          // 5. Finalized
    CANCELLED           // 6. Voided order/quote
}