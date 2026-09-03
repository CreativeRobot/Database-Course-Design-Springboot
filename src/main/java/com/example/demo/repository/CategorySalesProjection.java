package com.example.demo.repository;

import java.math.BigDecimal;

/**
 * CategorySalesProjection 数据访问接口，负责实体持久化及相关查询。
 */
public interface CategorySalesProjection {
    Long getCategoryId();

    String getCategoryName();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
