package com.brickwork.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_records")
@Data
public class InvoiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber; // e.g., INV-2026-0001

    @Column(unique = true, nullable = false)
    private String orderId;

    private Double totalAmount;
    private Double taxAmount;

    // EXTRA PIECE: Document Storage Strategy.
    // Instead of regenerating the PDF every time, we save it locally or to S3 and store the URL here.
    private String filePath;

    private LocalDateTime generatedDate = LocalDateTime.now();
}