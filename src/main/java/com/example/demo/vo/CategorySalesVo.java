package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategorySalesVo {
    private Long categoryId;
    private String categoryName;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
