package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityCommentVo {
    private Long id;
    private Long postId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long parentId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
