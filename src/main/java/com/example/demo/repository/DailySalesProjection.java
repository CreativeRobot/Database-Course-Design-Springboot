package com.example.demo.repository;

import java.math.BigDecimal;

public interface DailySalesProjection {
    String getSaleDate();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
