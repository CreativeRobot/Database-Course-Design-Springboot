package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.vo.UploadFileVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final Path uploadRoot;
    private final String publicUrlPrefix;
    private final long maxImageSize;

    public FileStorageService(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.public-url-prefix:/uploads}") String publicUrlPrefix,
            @Value("${app.upload.max-image-size:5242880}") long maxImageSize) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicUrlPrefix = normalizeUrlPrefix(publicUrlPrefix);
        this.maxImageSize = maxImageSize;
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(uploadRoot.resolve("covers"));
            Files.createDirectories(uploadRoot.resolve("avatars"));
        } catch (IOException exception) {
            throw new IllegalStateException("无法创建上传目录", exception);
        }
    }

    public UploadFileVo storeCover(MultipartFile file) {
        return storeImage(file, uploadRoot.resolve("covers"), "/covers");
    }

    public UploadFileVo storeAvatar(Long userId, MultipartFile file) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户信息不合法");
        }
        return storeImage(
                file,
                uploadRoot.resolve("avatars").resolve(userId.toString()),
                "/avatars/" + userId
        );
    }

    private UploadFileVo storeImage(MultipartFile file, Path directory, String publicPath) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > maxImageSize) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE, "图片大小不能超过5MB");
        }

        String contentType = file.getContentType();
        String normalizedContentType = contentType == null
                ? null
                : contentType.toLowerCase(Locale.ROOT);
        String extension = normalizedContentType == null
                ? null
                : IMAGE_EXTENSIONS.get(normalizedContentType);
        if (extension == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "只支持 JPG、PNG、GIF 和 WEBP 图片"
            );
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        Path target = normalizedDirectory.resolve(filename).normalize();
        if (!target.startsWith(normalizedDirectory)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "上传文件名不合法");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(normalizedDirectory);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "图片保存失败");
        }

        return new UploadFileVo(
                publicUrlPrefix + publicPath + "/" + filename,
                filename,
                normalizedContentType,
                file.getSize()
        );
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    private String normalizeUrlPrefix(String value) {
        String prefix = value == null || value.isBlank() ? "/uploads" : value.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix.endsWith("/")
                ? prefix.substring(0, prefix.length() - 1)
                : prefix;
    }
}
