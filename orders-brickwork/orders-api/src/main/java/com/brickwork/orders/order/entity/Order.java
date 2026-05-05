package com.brickwork.orders.order.entity;

import com.brickwork.orders.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    // We store only the ID of the customer, not a direct database link.
    // This is a core concept in microservices.
    //for authenticated users
    @Column
    private String customerId;

    // RESTORED PHASE 1: For Public Lead Generation (FUNC-005)
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // Saves the word (e.g., "DISPATCHED") in the DB, not a number
    @Column(nullable = false)
    private OrderStatus status;

    @Column(length = 500) // 500 characters for a full address
    private String deliveryAddress;

    // Phase 2: Enterprise Financial Tracking
    //Standard Total
    @Column
    private Double totalAmount;
    private Double totalProfit;
    private Double discountApplied;

    // This is a relationship to OrderDetail within the SAME service's database.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderDetails> orderDetails = new ArrayList<>();
}
