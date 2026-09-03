package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ForgotPasswordDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Valid
    @Size(min = 2, max = 2, message = "请回答两个密保问题")
    private List<SecurityQuestionAnswerDTO> answers;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须为6到50个字符")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
