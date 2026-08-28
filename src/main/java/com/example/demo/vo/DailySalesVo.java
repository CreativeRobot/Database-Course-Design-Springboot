package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailySalesVo {
    private String saleDate;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
