package com.brickwork.orders.analytics.repository;

import com.brickwork.orders.analytics.dto.PaymentMethodRevenueProjection;
import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductProjection;
import com.brickwork.orders.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Order, String> {

    //   --  TOTAL SALES AND  PROFIT  ANALYSIS ---

    // 1. YEARLY - Only paid / live orders (exclude PENDING_PAYMENT, QUOTE_REQUEST, CANCELLED)
    @Query(value = "SELECT YEAR(created_at) as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit, " +
            "COUNT(*) as total_orders " +
            "FROM orders WHERE status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY YEAR(created_at) ORDER BY YEAR(created_at) ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getYearlySalesAnalytics();

    // 2. MONTHLY - Only paid / live orders
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit, " +
            "COUNT(*) as total_orders " +
            "FROM orders WHERE status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY DATE_FORMAT(created_at, '%Y-%m') ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getMonthlySalesAnalytics();

    // 3. WEEKLY - Only paid / live orders
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-W%v') as period, " +
            "SUM(total_amount) as totalRevenue, SUM(total_profit) as totalProfit, " +
            "COUNT(*) as total_orders " +
            "FROM orders WHERE status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-W%v') ORDER BY DATE_FORMAT(created_at, '%Y-W%v') ASC", nativeQuery = true)
    List<SalesAnalyticsProjection> getWeeklySalesAnalytics();

    // --- TOP SELLING PRODUCTS ANALYTICS ---

    // 1. YEARLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold, " +
            // FIX: COALESCE aur snake_case alias
            "SUM(od.quantity * COALESCE(od.price_per_unit, 0)) as total_revenue_generated " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-%m'), od.product_id " +
            "ORDER BY DATE_FORMAT(o.created_at, '%Y-%m') DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getYearlyTopProducts();

    // 2. MONTHLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold, " +
            // FIX: COALESCE aur snake_case alias
            "SUM(od.quantity * COALESCE(od.price_per_unit, 0)) as total_revenue_generated " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-%m'), od.product_id " +
            "ORDER BY DATE_FORMAT(o.created_at, '%Y-%m') DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getMonthlyTopProducts();

    // 3. WEEKLY Top Products
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-W%v') as period, " +
            "od.product_id as productId, " +
            "SUM(od.quantity) as totalQuantitySold, " +
            // FIX: COALESCE aur snake_case alias
            "SUM(od.quantity * COALESCE(od.price_per_unit, 0)) as total_revenue_generated " +
            "FROM orders o " +
            "JOIN order_details od ON o.order_id = od.order_id " +
            "WHERE o.status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-W%v'), od.product_id " +
            "ORDER BY DATE_FORMAT(o.created_at, '%Y-W%v') DESC, totalQuantitySold DESC", nativeQuery = true)
    List<TopProductProjection> getWeeklyTopProducts();

    // Revenue by Payment Method for paid orders only
    @Query(value = "SELECT o.payment_method as paymentMethod, " +
            "SUM(o.total_amount) as totalRevenue, " +
            "COUNT(*) as orderCount " +
            "FROM orders o " +
            "WHERE o.status IN ('PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED') " +
            "AND o.payment_method IS NOT NULL " +
            "GROUP BY o.payment_method", nativeQuery = true)
    List<PaymentMethodRevenueProjection> getRevenueByPaymentMethod();
}

