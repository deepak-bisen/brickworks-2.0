package com.brickwork.products.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.brickwork.products.product.entity.Product;


@Entity
@Table(name = "PRODUCT_ATTACHMENT")
@Data
public class ProductAttachment {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @Column(name = "IMAGE_NAME", columnDefinition = "VARCHAR(200)", nullable = false)
    private String name;

    @Column(name = "EXTENSION", columnDefinition = "VARCHAR(200)",nullable = false)
    private String extension;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB",  nullable = false)
    private byte[] imageData;

}
