package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItemVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class CartItemVo {
    private Long id;
    private Long bookId;
    private String isbn;
    private String title;
    private String coverUrl;
    private BigDecimal salePrice;
    private Integer stock;
    private BookStatus bookStatus;
    private Boolean preSale;
    private LocalDateTime preSaleReleaseTime;
    private Integer quantity;
    private Boolean selected;
    private Boolean available;
    private BigDecimal subtotal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
