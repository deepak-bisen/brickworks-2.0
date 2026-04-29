package com.brickwork.orders.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
}
