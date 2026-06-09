package com.brickwork.finance.payment.entity;

import com.brickwork.finance.payment.enums.PaymentMethod;
import com.brickwork.finance.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "ORDER_ID", columnDefinition = "VARCHAR(40)", nullable = false, unique = true)
    private String orderId;

    @Column(name = "RAZORPAY_ORDER_ID", columnDefinition = "VARCHAR(40)", nullable = true)
    private String razorpayOrderId;

    @Column(name = "RAZORPAY_PAYMENT_ID", columnDefinition = "VARCHAR(40)", nullable = true)
    private String razorpayPaymentId;

    @Column(name = "AMOUNT", columnDefinition = "DOUBLE", nullable = false)
    private Double amount;

    @Column(name = "CURRENCY", columnDefinition = "VARCHAR(5)", nullable = false)
    private String currency = "INR";

    @Column(name = "UTR_NUMBER", columnDefinition = "VARCHAR(15)", nullable = true)
    private String utrNumber;

    @Column(name = "PAYMENT_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "PAYMENT_METHOD", nullable = true)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "CREATED_AT", nullable = false )
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }


}