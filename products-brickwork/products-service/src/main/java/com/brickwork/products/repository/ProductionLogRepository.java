package com.brickwork.products.repository;

import com.brickwork.products.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, String> {
}