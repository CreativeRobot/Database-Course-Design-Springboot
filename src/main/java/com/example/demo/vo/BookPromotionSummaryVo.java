package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookPromotionSummaryVo {
    private Long id;
    private String name;
    private String description;
    private Integer discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
