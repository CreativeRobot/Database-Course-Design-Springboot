package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVo {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean preSale;
    private LocalDateTime preSaleReleaseTime;
}
