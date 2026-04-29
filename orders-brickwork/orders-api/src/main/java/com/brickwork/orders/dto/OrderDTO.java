package com.brickwork.orders.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    private String orderId;
    private String customerId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalCost;
    private String deliveryAddress;

    // we might want to include OrderDetailsDTOs here too if needed for the full view
    // private List<OrderDetailsDTO> orderDetails;
}