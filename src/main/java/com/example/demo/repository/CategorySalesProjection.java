package com.example.demo.repository;

import java.math.BigDecimal;

public interface CategorySalesProjection {
    Long getCategoryId();

    String getCategoryName();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
