package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVo {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
