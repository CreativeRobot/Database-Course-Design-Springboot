package com.example.demo.dto;

import com.example.demo.entity.RefundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * CreateRefundRequestDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class CreateRefundRequestDTO {
    private Long orderId;
    @NotNull private Long orderItemId;
    @NotNull private RefundType type;
    @NotNull @Positive private Integer quantity;
    @NotBlank private String reason;
}

