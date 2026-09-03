package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommunityPostVo {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> imageUrls = new ArrayList<>();
    private List<Long> bookIds = new ArrayList<>();
    private List<String> bookTitles = new ArrayList<>();
    private long commentCount;
}
