package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

import java.math.BigDecimal;

/**
 * RecommendationBookVo 响应视图对象，用于封装返回给客户端的数据。
 */
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
