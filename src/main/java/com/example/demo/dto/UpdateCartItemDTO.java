package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * UpdateCartItemDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class UpdateCartItemDTO {

    @Min(value = 1, message = "购买数量至少为1")
    @Max(value = 999, message = "单种图书最多购买999本")
    private Integer quantity;

    private Boolean selected;
}
