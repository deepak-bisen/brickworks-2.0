package com.brickwork.orders.order.repository;

import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductProjection;
import com.brickwork.orders.order.entity.Order;
import com.brickwork.orders.order.enums.OrderStatus;
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


    //   --  TOTAL SALES AND  PROFIT  ANALYSIS ---

    // 1. YEARLY
    @Query(value = "SELECT YEAR(created_at) as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit " +
            "FROM orders WHERE status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY YEAR(order_date) ORDER BY period ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getYearlySalesAnalytics();

    // 2. MONTHLY
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit " +
            "FROM orders WHERE status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY period ORDER BY period ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getMonthlySalesAnalytics();

    // 3. WEEKLY
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-W%v') as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit " +
            "FROM orders WHERE status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY period ORDER BY period ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getWeeklySalesAnalytics();


    // --- TOP SELLING PRODUCTS ANALYTICS ---

    // 1. YEARLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY period, productId " +
            "ORDER BY period DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getYearlyTopProducts();

    // 2. MONTHLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY period, productId " +
            "ORDER BY period DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getMonthlyTopProducts();

    // 3. WEEKLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-W%v') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status NOT IN ('PENDING', 'CANCELLED') " +
            "GROUP BY period, productId " +
            "ORDER BY period DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getWeeklyTopProducts();
}
