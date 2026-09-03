package com.example.demo.dto;

import com.example.demo.entity.BookPromotionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookPromotionStatusDTO {
    @NotNull(message = "状态不能为空")
    private BookPromotionStatus status;
}
