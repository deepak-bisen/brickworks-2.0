package com.brickwork.finance.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_records")
@Data
public class RefundRecord {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    @Column(name = "RAZORPAY_REFUND_ID", columnDefinition = "VARCHAR(40)", unique = true)
    private String razorpayRefundId;

    @Column(name = "AMOUNT",  columnDefinition = "DOUBLE", nullable = false)
    private Double amount;

    @Column(name = "REASON",  columnDefinition = "VARCHAR(255)", nullable = false)
    private String reason;

    @Column(name = "REFUND_STATUS",  columnDefinition = "VARCHAR(15)", nullable = false)
    private String refundStatus; // PENDING, PROCESSED

    @Column(name = "PROCESSED_AT",  nullable = false )
    private LocalDateTime processedAt = LocalDateTime.now();
}