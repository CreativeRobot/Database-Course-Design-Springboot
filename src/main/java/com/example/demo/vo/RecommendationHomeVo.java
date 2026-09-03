package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 首页推荐分页响应。
 */
@Data
@AllArgsConstructor
public class RecommendationHomeVo {
    private String source;
    private List<RecommendationBookVo> books;
    private int page;
    private int size;
    private boolean hasMore;

    public RecommendationHomeVo(String source, List<RecommendationBookVo> books) {
        this(source, books, 1, books == null ? 0 : books.size(), false);
    }

    /**
     * 保留 JavaBean 风格的 getHasMore，方便测试和序列化客户端统一读取。
     */
    public boolean getHasMore() {
        return hasMore;
    }
}
