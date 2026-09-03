package com.example.demo.dto;

import com.example.demo.entity.BookBundleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookBundleStatusDTO {
    @NotNull(message = "状态不能为空")
    private BookBundleStatus status;
}
