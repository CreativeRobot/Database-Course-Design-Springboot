package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书详情视图对象，含作者与分类
 */
@Data
public class BookDetailVo {
    private Long id;
    private String isbn;
    private String title;
    private Long publisherId;
    private String publisherName;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal baseSalePrice;
    private BookPromotionSummaryVo promotion;
    private Integer stock;
    private Boolean preSale;
    private LocalDateTime preSaleReleaseTime;
    private LocalDate publishDate;
    private String edition;
    private Integer pages;
    private String description;
    private String coverUrl;
    private BookStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AuthorItem> authors;
    private List<CategoryItem> categories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorItem {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {
        private Long id;
        private String name;
    }
}
