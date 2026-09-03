package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CreateOrderDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class CreateOrderDTO {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @Size(max = 255, message = "订单备注不能超过255个字符")
    private String remark;
}
