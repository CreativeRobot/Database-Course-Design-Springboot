package com.example.demo.repository;

import java.math.BigDecimal;

/**
 * DailySalesProjection 数据访问接口，负责实体持久化及相关查询。
 */
public interface DailySalesProjection {
    String getSaleDate();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
