package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AdminStatisticsVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class AdminStatisticsVo {
    private Long completedOrderCount;
    private BigDecimal salesAmount;
    private Long soldQuantity;
    private List<MonthlySalesVo> monthlySales;
    private List<DailySalesVo> dailySales;
    private List<TopBookSalesVo> topBooks;
    private List<CategorySalesVo> categorySales;
    private List<LowStockBookVo> lowStockBooks;
}
