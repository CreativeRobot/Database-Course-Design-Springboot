package com.example.demo.repository;

import java.math.BigDecimal;

/**
 * TopBookSalesProjection 数据访问接口，负责实体持久化及相关查询。
 */
public interface TopBookSalesProjection {
    Long getBookId();

    String getBookTitle();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
