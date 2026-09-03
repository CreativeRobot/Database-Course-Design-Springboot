package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UserStatusDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class UserStatusDTO {

    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态只能为0或1")
    @Max(value = 1, message = "用户状态只能为0或1")
    private Integer status;
}
