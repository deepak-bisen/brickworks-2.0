package com.brickwork.users.enums;

public enum Role {
    ADMIN,          // Business Owner (Can manage products, see all orders, finance)
    CUSTOMER,     // Regular Customer (Can place orders, view own history)
    STAFF           // Factory/Delivery workers (Can update order status into Dispatched/Delivered)
}
