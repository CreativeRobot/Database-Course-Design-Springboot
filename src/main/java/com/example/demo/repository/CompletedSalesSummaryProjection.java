package com.example.demo.repository;

import java.math.BigDecimal;

/**
 * CompletedSalesSummaryProjection 数据访问接口，负责实体持久化及相关查询。
 */
public interface CompletedSalesSummaryProjection {
    Long getCompletedOrderCount();

    BigDecimal getSalesAmount();
}
