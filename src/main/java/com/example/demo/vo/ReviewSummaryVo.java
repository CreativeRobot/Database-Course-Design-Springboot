package com.example.demo.vo;

import lombok.Data;

@Data
public class ReviewSummaryVo {
    private Long bookId;
    private Double averageRating;
    private Long reviewCount;
    private PageVo<ReviewVo> reviews;
}
