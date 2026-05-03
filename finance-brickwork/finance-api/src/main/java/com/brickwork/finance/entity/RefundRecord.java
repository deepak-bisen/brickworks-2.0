package com.brickwork.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_records")
@Data
public class RefundRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    @Column(unique = true)
    private String razorpayRefundId;

    private Double amount;
    private String reason;
    private String status; // PENDING, PROCESSED

    private LocalDateTime processedAt = LocalDateTime.now();
}