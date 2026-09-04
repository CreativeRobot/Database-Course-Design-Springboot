package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItemVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class OrderItemVo {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal paidSubtotal;
    private Boolean preSale;
    private LocalDateTime preSaleReleaseTime;
    private Integer bundleCoveredQuantity;
    private Integer standaloneRefundableQuantity;
    private Integer approvedStandaloneQuantity;
    private Integer pendingStandaloneQuantity;
}


