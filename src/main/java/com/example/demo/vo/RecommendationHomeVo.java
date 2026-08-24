package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecommendationHomeVo {
    private String source;
    private List<RecommendationBookVo> books;
}
