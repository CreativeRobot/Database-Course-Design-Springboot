package com.example.demo.dto;

import com.example.demo.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * PayOrderDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class PayOrderDTO {

    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;
}
