package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DailySalesVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class DailySalesVo {
    private String saleDate;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
