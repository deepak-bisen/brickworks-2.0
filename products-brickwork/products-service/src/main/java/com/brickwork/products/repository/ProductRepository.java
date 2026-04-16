package com.brickwork.products.repository;

import com.brickwork.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,String> {
    // Phase 1 Search logic preserved
    java.util.List<Product> findByCategory(String category);
}