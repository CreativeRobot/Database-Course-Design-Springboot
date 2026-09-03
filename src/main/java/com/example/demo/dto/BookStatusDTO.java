package com.example.demo.dto;

import com.example.demo.entity.BookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * BookStatusDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class BookStatusDTO {
    @NotNull(message = "状态不能为空")
    private BookStatus status;
}
