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
    @Column(name = "ORDER_ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    // We store only the ID of the customer, not a direct database link.
    // This is a core concept in microservices.
    //for authenticated users
    @Column(name = "CUSTOMER_ID", columnDefinition = "VARCHAR(40)", nullable = true)
    private String customerId;

    // RESTORED PHASE 1: For Public Lead Generation (FUNC-005)
    @Column(name = "CUSTOMER_NAME", columnDefinition = "VARCHAR(30)", nullable = false)
    private String customerName;

    @Column(name = "CUSTOMER_PHONE", columnDefinition = "VARCHAR(13)", nullable = false)
    private String customerPhone;

    @Column(name = "CUSTOMER_EMAIL", columnDefinition = "VARCHAR(30)", nullable = false)
    private String customerEmail;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // Saves the word (e.g., "DISPATCHED") in the DB, not a number
    @Column(name = "STATUS", nullable = false)
    private OrderStatus status;

    @Column(name = "DELIVERY_ADDRESS", columnDefinition = "VARCHAR(500)") // 500 characters for a full address
    private String deliveryAddress;

    // Phase 2: Enterprise Financial Tracking
    @Column(name = "GROSS_AMOUNT", columnDefinition = "DOUBLE",nullable = false)
    private Double grossAmount; // Amount before discount

    @Column(name = "TOTAL_AMOUNT", columnDefinition = "DOUBLE",nullable = false)
    private Double totalAmount; // Amount after discount

    @Column(name = "GROSS_PROFIT", columnDefinition = "DOUBLE")
    private Double grossProfit; // totalAmount - totalCost (before GST adjustment)

    @Column(name = "TOTAL_PROFIT", columnDefinition = "DOUBLE",nullable = false)
    private Double totalProfit; // Net profit after (totalAmount / 1.18) - totalCost

    @Column(name = "PAYMENT_METHOD")
    private String paymentMethod; // CASH_ON_DELIVERY, BANK_TRANSFER, ONLINE - set on payment confirmation

    @Column(name = "DISCOUNT_APPLIED", columnDefinition = "DOUBLE",nullable = false)
    private Double discountApplied;

    // Notification visibility & resend support
    @Column(name = "LAST_NOTIFICATION_SENT_AT")
    private LocalDateTime lastNotificationSentAt;

    @Column(name = "LAST_NOTIFICATION_STATUS", columnDefinition = "VARCHAR(500)")
    private String lastNotificationStatus; // e.g. "CONFIRMATION_EMAIL_SENT|WHATSAPP_SENT" or "DISPATCH_SENT|EMAIL_FAILED"

    // This is a relationship to OrderDetail within the SAME service's database.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderDetails> orderDetails = new ArrayList<>();
}
