package com.brickwork.orders.order.repository;

import com.brickwork.orders.entity.Order;
import com.brickwork.orders.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,String> {

    // RESTORED PHASE 1: Fetch orders for the Customer Portal (FUNC-009)
    List<Order> findByCustomerId(String customerId);

    // 1. Fetch ONLY Quotes (For the Sales Team to follow up on)
    List<Order> findByStatus(OrderStatus status);

    // 2. Fetch ALL REAL Orders (Everything EXCEPT Quotes) - The Safest Query!
    List<Order> findByStatusNot(OrderStatus status);

    // 3. Example of a financial calculation query directly in the database
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'QUOTE_REQUEST' AND o.status != 'CANCELLED'")
    Double calculateTotalRealizedRevenue();
}
