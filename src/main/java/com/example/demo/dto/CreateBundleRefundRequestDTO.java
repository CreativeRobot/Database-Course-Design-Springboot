package com.example.demo.dto;

import com.example.demo.entity.RefundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBundleRefundRequestDTO {
    private Long bundleApplicationId;
    @NotNull private RefundType type;
    @NotBlank private String reason;
}
