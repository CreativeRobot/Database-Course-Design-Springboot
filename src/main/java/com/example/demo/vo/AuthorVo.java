package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthorVo {
    private Long id;
    private String name;
    private String country;
    private String introduction;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
