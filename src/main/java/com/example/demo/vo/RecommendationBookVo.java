package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendationBookVo {
    private Long id;
    private String isbn;
    private String title;
    private Long publisherId;
    private String publisherName;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private BookStatus status;
    private String coverUrl;
    private String reason;
}
