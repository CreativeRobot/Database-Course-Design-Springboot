package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommunityPostStatusDTO {
    @NotNull(message = "帖子状态不能为空")
    @Min(value = 0, message = "帖子状态只能为0或1")
    @Max(value = 1, message = "帖子状态只能为0或1")
    private Integer status;
}
