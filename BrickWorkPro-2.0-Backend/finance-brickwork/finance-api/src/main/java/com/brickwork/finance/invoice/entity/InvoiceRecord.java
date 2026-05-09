package com.brickwork.finance.invoice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_records")
@Data
public class InvoiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String invoiceId;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @Column(unique = true, nullable = false)
    private String orderId;

    private Double totalAmount;
    private Double taxAmount;
    private String filePath; // Path where PDF is stored

    private LocalDateTime generatedDate = LocalDateTime.now();
}