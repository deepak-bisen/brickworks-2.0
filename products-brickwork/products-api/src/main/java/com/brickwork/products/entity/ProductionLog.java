package com.brickwork.products.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "production_logs")
public class ProductionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String productionLogId;

    private String managerId; // Tracks which employee created the log

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // Nullable if logging raw materials instead of bricks

    private String stage; // "MOLDED", "IN_KILN", "BAKED"
    private Integer quantity;
    private LocalDate logDate;
}
