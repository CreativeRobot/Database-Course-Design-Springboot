package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AuthorVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class AuthorVo {
    private Long id;
    private String name;
    private String country;
    private String introduction;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
