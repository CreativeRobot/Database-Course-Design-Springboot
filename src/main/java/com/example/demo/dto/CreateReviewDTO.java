package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CreateReviewDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class CreateReviewDTO {

    @NotNull(message = "订单明细不能为空")
    private Long orderItemId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer rating;

    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String content;
}
