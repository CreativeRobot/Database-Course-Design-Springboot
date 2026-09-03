package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SaveCategoryDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class SaveCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String name;

    private Long parentId;

    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder = 0;

    @Min(value = 0, message = "分类状态只能为0或1")
    @Max(value = 1, message = "分类状态只能为0或1")
    private Integer status = 1;
}
