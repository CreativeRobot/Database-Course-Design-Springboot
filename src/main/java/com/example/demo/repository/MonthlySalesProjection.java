package com.example.demo.repository;

import java.math.BigDecimal;

/**
 * MonthlySalesProjection 数据访问接口，负责实体持久化及相关查询。
 */
public interface MonthlySalesProjection {
    String getSaleMonth();

    Long getCompletedOrderCount();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
