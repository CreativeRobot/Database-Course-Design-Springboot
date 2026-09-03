package com.example.demo.vo;

import lombok.Data;

/**
 * ReviewSummaryVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class ReviewSummaryVo {
    private Long bookId;
    private Double averageRating;
    private Long reviewCount;
    private PageVo<ReviewVo> reviews;
}
