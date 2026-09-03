package com.example.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UploadFileVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileVo {
    private String url;
    private String filename;
    private String contentType;
    private long size;
}
