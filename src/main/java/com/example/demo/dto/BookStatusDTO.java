package com.example.demo.dto;

import com.example.demo.entity.BookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookStatusDTO {
    @NotNull(message = "状态不能为空")
    private BookStatus status;
}
