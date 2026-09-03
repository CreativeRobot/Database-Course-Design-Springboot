package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SaveUserAddressDTO 请求数据传输对象，用于接收和校验接口输入参数。
 */
@Data
public class SaveUserAddressDTO {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货手机号不能为空")
    @Size(max = 20, message = "收货手机号不能超过20个字符")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份不能超过50个字符")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市不能超过50个字符")
    private String city;

    @Size(max = 50, message = "区县不能超过50个字符")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String detailAddress;

    @Size(max = 10, message = "邮政编码不能超过10个字符")
    private String postalCode;

    /** 新增时不传表示非默认；修改时不传表示保持原状态。 */
    private Boolean defaultAddress;
}
