package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookBundleItemVo {
    private Long bookId;
    private String title;
    private String isbn;
    private String coverUrl;
    private BigDecimal salePrice;
    private Integer stock;
    private String bookStatus;
}
