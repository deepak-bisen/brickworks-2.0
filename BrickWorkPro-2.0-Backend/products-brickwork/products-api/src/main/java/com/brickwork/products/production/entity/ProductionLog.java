package com.brickwork.products.production.entity;

import com.brickwork.products.product.entity.Product;
import com.brickwork.products.production.enums.ProductionStage;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "production_logs")
public class ProductionLog {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "MANAGER_ID", columnDefinition = "VARCHAR(40)", nullable = false)
    private String managerId; // Tracks which employee created the log

    @Column(name = "MANAGER_NAME", columnDefinition = "VARCHAR(25)", nullable = false)
    private String managerName; // Stores the display name of the staff who created the log

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // Nullable if logging raw materials instead of bricks

    @Column(name = "STAGE",  nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionStage stage;// "MOLDED", "IN_KILN", "BAKED"

    @Column(name = "QUANTITY", columnDefinition = "INT", nullable = false)
    private Integer quantity;

    @Column(name = "CREATED_AT", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ORDER_ID", columnDefinition = "VARCHAR(40)")
    private String orderId;
}
