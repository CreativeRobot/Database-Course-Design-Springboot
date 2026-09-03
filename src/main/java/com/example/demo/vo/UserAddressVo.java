package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * UserAddressVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class UserAddressVo {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String postalCode;
    private Boolean defaultAddress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
