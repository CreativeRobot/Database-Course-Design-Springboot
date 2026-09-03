package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CaptchaVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVo {
    private String captchaId;
    private String imageBase64;
    private int expiresInSeconds;
}
