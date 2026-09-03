package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBookPromotionDTO {
    @NotNull(message = "图书不能为空")
    private Long bookId;
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称不能超过100个字符")
    private String name;
    @Size(max = 500, message = "活动说明不能超过500个字符")
    private String description;
    @NotNull(message = "折扣不能为空")
    @Min(value = 1, message = "折扣必须为1到99之间的整数")
    @Max(value = 99, message = "折扣必须为1到99之间的整数")
    private Integer discountPercent;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
