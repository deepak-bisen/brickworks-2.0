package com.brickwork.finance.invoice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_records")
@Data
public class InvoiceRecord {
    @Id
    @Column(name = "INVOICE_ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "INVOICE_NUMBER", columnDefinition = "VARCHAR(20)", unique = true, nullable = false)
    private String invoiceNumber;

    @Column(name ="ORDER_ID", unique = true, nullable = false)
    private String orderId;

    @Column(name = "TOTAL_AMOUNT", columnDefinition = "DOUBLE",nullable = false)
    private Double totalAmount;

    @Column(name = "TAX_AMOUNT", columnDefinition = "DOUBLE",nullable = false)
    private Double taxAmount;

    @Column(name = "FILE_PATH", columnDefinition = "VARCHAR(200)",nullable = false)
    private String filePath; // Path where PDF is stored

    @Column(name = "GENERATED_DATE", columnDefinition = "DATETIME",nullable = false)
    private LocalDateTime generatedDate = LocalDateTime.now();
}