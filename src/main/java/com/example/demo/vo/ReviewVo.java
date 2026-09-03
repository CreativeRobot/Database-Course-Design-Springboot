package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ReviewVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class ReviewVo {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long orderItemId;
    private Long userId;
    private String reviewerName;
    private Integer rating;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
