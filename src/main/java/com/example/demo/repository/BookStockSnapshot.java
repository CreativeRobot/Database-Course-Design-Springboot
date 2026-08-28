package com.example.demo.repository;

import java.math.BigDecimal;

/** Scalar database snapshot used while a book row is locked for stock mutation. */
public interface BookStockSnapshot {
    Long getId();

    Integer getStock();

    String getStatus();

    BigDecimal getSalePrice();

    String getTitle();

    String getIsbn();
}
