package com.brickwork.orders.analytics.service.impl;

import com.brickwork.orders.client.ProductClient;
import com.brickwork.orders.order.dto.ProductDTO;
import com.brickwork.orders.analytics.dto.SalesAnalyticsProjection;
import com.brickwork.orders.analytics.dto.TopProductProjection;
import com.brickwork.orders.analytics.dto.TopProductResponseDTO;
import com.brickwork.orders.order.repository.OrderRepository;
import com.brickwork.orders.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

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
    public List<TopProductResponseDTO> getTopProducts(String timeframe) {

        // 1. Get raw database grouped data (Projections)
        List<TopProductProjection> projections = switch (timeframe.toLowerCase()) {
            case "yearly" -> orderRepository.getYearlyTopProducts();
            case "monthly" -> orderRepository.getMonthlyTopProducts();
            case "weekly" -> orderRepository.getWeeklyTopProducts();
            default -> throw new IllegalArgumentException("Invalid timeframe. Use 'yearly', 'monthly', or 'weekly'.");
        };

        // 2. Loop through and fetch names via Feign
        return projections.stream().map(proj -> {
            TopProductResponseDTO dto = new TopProductResponseDTO();
            dto.setPeriod(proj.getPeriod());
            dto.setProductId(proj.getProductId());
            dto.setTotalQuantitySold(proj.getTotalQuantitySold());

            try {
                // Fetch the product from Products Service!
                ProductDTO product = productClient.getProductById(proj.getProductId());
                if (product != null && product.getName() != null) {
                    dto.setProductName(product.getName());
                } else {
                    dto.setProductName("Unknown Product");
                }
            } catch (Exception e) {
                // Fallback just in case the Products Service is down or product was deleted
                dto.setProductName("Unknown (Service Unavailable)");
            }
            return dto;
        }).collect(Collectors.toList());
    }
}