package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVo {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
