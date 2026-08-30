package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书列表项视图对象
 */
@Data
public class BookVo {
    private Long id;
    private String isbn;
    private String title;
    private Long publisherId;
    private String publisherName;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private Boolean preSale;
    private LocalDateTime preSaleReleaseTime;
    private BookStatus status;
    private String coverUrl;
}
