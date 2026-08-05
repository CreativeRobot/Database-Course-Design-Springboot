package com.example.demo.repository;

import java.math.BigDecimal;

public interface TopBookSalesProjection {
    Long getBookId();

    String getBookTitle();

    Long getSoldQuantity();

    BigDecimal getSalesAmount();
}
