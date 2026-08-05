package com.example.demo.repository;

import java.math.BigDecimal;

public interface MonthlySalesProjection {
    String getSaleMonth();

    Long getCompletedOrderCount();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
