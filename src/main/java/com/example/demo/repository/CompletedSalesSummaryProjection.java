package com.example.demo.repository;

import java.math.BigDecimal;

public interface CompletedSalesSummaryProjection {
    Long getCompletedOrderCount();

    BigDecimal getSalesAmount();
}
