package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SaveAuthorDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class SaveAuthorDTO {

    @NotBlank(message = "作者姓名不能为空")
    @Size(max = 100, message = "作者姓名不能超过100个字符")
    private String name;

    @Size(max = 50, message = "国家或地区不能超过50个字符")
    private String country;

    @Size(max = 1000, message = "作者简介不能超过1000个字符")
    private String introduction;
}
