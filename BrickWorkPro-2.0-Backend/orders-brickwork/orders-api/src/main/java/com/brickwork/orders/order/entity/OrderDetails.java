package com.brickwork.orders.order.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ORDER_DETAILS")
public class OrderDetails {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // This defines the "many" side of the relationship. Many OrderDetail items belong to one Order.
    // fetch = FetchType.LAZY means this data is only loaded from the DB when it's explicitly asked for.
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    // We store the ID of the product from the product-service.
    @Column(name = "PRODUCT_ID", columnDefinition = "VARCHAR(50)", nullable = false)
    private String productId;

    @Column(name = "PRODUCT_NAME", columnDefinition = "VARCHAR(50)",  nullable = false)
    private String productName;

    @Column(name = "QUANTITY", columnDefinition = "INT", nullable = false)
    private int quantity;

    // We store the price here to capture the price at the time of the order,
    // in case the product's price changes in the future.
    @Column(name = "PRICE_PER_UNIT", columnDefinition = "DOUBLE", nullable = false)
    private Double pricePerUnit;

}
