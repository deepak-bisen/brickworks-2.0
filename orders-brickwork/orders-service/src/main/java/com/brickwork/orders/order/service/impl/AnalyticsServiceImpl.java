package com.brickwork.orders.order.service.impl;

import com.brickwork.orders.dto.SalesAnalyticsProjection;
import com.brickwork.orders.dto.TopProductProjection;
import com.brickwork.orders.order.repository.OrderRepository;
import com.brickwork.orders.order.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<SalesAnalyticsProjection> getSalesData(String timeframe) {
        return switch (timeframe.toLowerCase()) {
            case "yearly" -> orderRepository.getYearlySalesAnalytics();
            case "monthly" -> orderRepository.getMonthlySalesAnalytics();
            case "weekly" -> orderRepository.getWeeklySalesAnalytics();
            default -> throw new IllegalArgumentException("Invalid timeframe. Use 'yearly', 'monthly', or 'weekly'.");
        };
    }

    @Override
    public List<TopProductProjection> getTopProducts(String timeframe) {
        return switch (timeframe.toLowerCase()) {
            case "yearly" -> orderRepository.getYearlyTopProducts();
            case "monthly" -> orderRepository.getMonthlyTopProducts();
            case "weekly" -> orderRepository.getWeeklyTopProducts();
            default -> throw new IllegalArgumentException("Invalid timeframe. Use 'yearly', 'monthly', or 'weekly'.");
        };
    }
}