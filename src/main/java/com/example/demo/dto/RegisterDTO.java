package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * RegisterDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度必须为3到30个字符")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须为6到50个字符")
    private String password;

    @Size(max = 30, message = "昵称不能超过30个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    @Pattern(
            regexp = "^$|1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String phone;

    @NotBlank(message = "验证码标识不能为空")
    @Size(max = 64, message = "验证码标识不正确")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    @Size(max = 16, message = "验证码长度不正确")
    private String captchaCode;

    @Valid
    @NotNull(message = "请设置密保问题")
    @Size(min = 3, max = 3, message = "请设置三个密保问题")
    private List<SecurityQuestionAnswerDTO> securityQuestions;
}
