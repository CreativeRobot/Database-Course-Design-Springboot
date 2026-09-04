package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BundleRefundRequestItemVo {
    private Long orderItemId;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private BigDecimal salePrice;
    private BigDecimal allocatedDiscount;
    private Integer quantity;
    private BigDecimal amount;
}
