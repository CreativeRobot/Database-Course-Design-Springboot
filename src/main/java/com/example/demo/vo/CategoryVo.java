package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class CategoryVo {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private List<CategoryVo> children = new ArrayList<>();
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}