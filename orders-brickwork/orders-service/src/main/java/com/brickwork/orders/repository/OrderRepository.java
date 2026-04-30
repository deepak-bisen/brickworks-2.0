package com.brickwork.orders.repository;

import com.brickwork.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,String> {

    // RESTORED PHASE 1: Fetch orders for the Customer Portal (FUNC-009)
    List<Order> findByCustomerId(String customerId);
}
