package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopBookSalesVo {
    private Long bookId;
    private String bookTitle;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
