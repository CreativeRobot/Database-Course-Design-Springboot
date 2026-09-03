package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AddCartItemDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class AddCartItemDTO {

    @NotNull(message = "图书不能为空")
    private Long bookId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    @Max(value = 999, message = "单种图书最多购买999本")
    private Integer quantity = 1;
}
