package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublisherVo {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String introduction;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
