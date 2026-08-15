package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewStatusDTO {

    @NotNull(message = "评价状态不能为空")
    @Min(value = 0, message = "评价状态只能为0或1")
    @Max(value = 1, message = "评价状态只能为0或1")
    private Integer status;
}
