package com.example.demo.dto;

import com.example.demo.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayOrderDTO {

    @NotNull(message = "支付方式不能为空")
    private PaymentMethod paymentMethod;
}
