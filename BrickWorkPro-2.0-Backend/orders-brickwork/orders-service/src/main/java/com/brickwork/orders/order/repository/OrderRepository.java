package com.brickwork.orders.order.repository;

import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductProjection;
import com.brickwork.orders.order.entity.Order;
import com.brickwork.orders.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {

    // RESTORED PHASE 1: Fetch orders for the Customer Portal

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    // 1. Fetch ONLY Quotes (For the Sales Team to follow up on)
    List<Order> findByStatus(OrderStatus status);

    // 2. Fetch ALL REAL Orders (Everything EXCEPT Quotes) - sorted newest first by default
    List<Order> findByStatusNotOrderByCreatedAtDesc(OrderStatus status);

    // 3. Example of a financial calculation query directly in the database
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'QUOTE_REQUEST' AND o.status != 'CANCELLED'")
    Double calculateTotalRealizedRevenue();

    // Quotes ko chhod kar baaki saare actual orders count karne ke liye
    long countByStatusNot(OrderStatus status);

}