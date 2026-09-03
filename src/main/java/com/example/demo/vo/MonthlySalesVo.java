package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * MonthlySalesVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class MonthlySalesVo {
    private String saleMonth;
    private Long completedOrderCount;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
