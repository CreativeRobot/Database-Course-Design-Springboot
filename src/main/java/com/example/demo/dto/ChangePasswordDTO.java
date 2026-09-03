package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ChangePasswordDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class ChangePasswordDTO {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度必须为6到50个字符")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 50, message = "确认密码长度必须为6到50个字符")
    private String confirmPassword;
}
