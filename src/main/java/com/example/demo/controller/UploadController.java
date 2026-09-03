package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FileStorageService;
import com.example.demo.vo.UploadFileVo;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UploadFileVo> uploadImage(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") @NotNull MultipartFile file) {
        return Result.success(fileStorageService.storePostImage(userId, file));
    }
}
