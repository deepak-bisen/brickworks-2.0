package com.brickwork.products.production.repository;

import com.brickwork.products.production.dto.ProductionAnalyticsProjection;
import com.brickwork.products.production.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, String> {

    // -- FACTORY PRODUCTION ANALYTICS --


    @Query(value = "SELECT YEAR(created_at) as period, stage, SUM(quantity) as totalQuantity " +
            "FROM production_logs GROUP BY period, stage ORDER BY period ASC", nativeQuery = true)
    List<ProductionAnalyticsProjection> getYearlyProductionAnalytics();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, stage, SUM(quantity) as totalQuantity " +
            "FROM production_logs GROUP BY period, stage ORDER BY period ASC", nativeQuery = true)
    List<ProductionAnalyticsProjection> getMonthlyProductionAnalytics();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-W%v') as period, stage, SUM(quantity) as totalQuantity " +
            "FROM production_logs GROUP BY period, stage ORDER BY period ASC", nativeQuery = true)
    List<ProductionAnalyticsProjection> getWeeklyProductionAnalytics();
}