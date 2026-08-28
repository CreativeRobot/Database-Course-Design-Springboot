package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
