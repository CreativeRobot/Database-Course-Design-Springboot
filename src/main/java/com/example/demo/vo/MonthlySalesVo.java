package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlySalesVo {
    private String saleMonth;
    private Long completedOrderCount;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
