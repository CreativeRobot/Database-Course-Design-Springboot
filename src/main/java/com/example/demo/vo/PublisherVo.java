package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * PublisherVo 响应视图对象，用于封装返回给客户端的数据。
 */
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
