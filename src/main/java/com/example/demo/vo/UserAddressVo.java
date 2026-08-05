package com.example.demo.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
