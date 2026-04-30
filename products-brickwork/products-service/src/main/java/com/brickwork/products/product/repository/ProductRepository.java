package com.brickwork.products.product.repository;

import com.brickwork.products.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,String> {
    // Phase 1 Search logic preserved
    java.util.List<Product> findByCategory(String category);
}