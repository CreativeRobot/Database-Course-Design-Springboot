package com.example.demo.vo;

import com.example.demo.entity.BookPromotionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookPromotionVo extends BookPromotionSummaryVo {
    private Long bookId;
    private String bookTitle;
    private String coverUrl;
    private BigDecimal baseSalePrice;
    private BigDecimal promotionPrice;
    private BookPromotionStatus status;
    private Long version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
