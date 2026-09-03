package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FileStorageService;
import com.example.demo.vo.UploadFileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * AdminUploadController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    // ==================== 接口定义 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UploadFileVo> uploadImage(
            @RequestParam("file") MultipartFile file) {
        return Result.success(fileStorageService.storeCover(file));
    }
}
