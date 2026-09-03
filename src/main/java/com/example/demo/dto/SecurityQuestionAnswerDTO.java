package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SecurityQuestionAnswerDTO {
    @NotBlank(message = "密保问题不能为空")
    private String questionKey;

    @NotBlank(message = "密保答案不能为空")
    private String answer;
}
