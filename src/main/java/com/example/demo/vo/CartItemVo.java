package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer quantity;
    private Boolean selected;
    private Boolean available;
    private BigDecimal subtotal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
