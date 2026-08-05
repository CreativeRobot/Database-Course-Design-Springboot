package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavePublisherDTO {

    @NotBlank(message = "出版社名称不能为空")
    @Size(max = 100, message = "出版社名称不能超过100个字符")
    private String name;

    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "出版社地址不能超过255个字符")
    private String address;

    @Size(max = 500, message = "出版社简介不能超过500个字符")
    private String introduction;
}
