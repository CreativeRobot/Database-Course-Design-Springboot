package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * TopBookSalesVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class TopBookSalesVo {
    private Long bookId;
    private String bookTitle;
    private Long soldQuantity;
    private BigDecimal salesAmount;
}
