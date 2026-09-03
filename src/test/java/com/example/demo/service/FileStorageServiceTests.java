package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.vo.UploadFileVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTests {

    @TempDir
    Path uploadRoot;

    @Test
    void storesAvatarInTheCurrentUsersDirectory() throws Exception {
        FileStorageService storage = new FileStorageService(
                uploadRoot.toString(), "/uploads", 1024 * 1024);
        storage.initialize();
        MockMultipartFile image = new MockMultipartFile(
                "file", "portrait.png", "image/png", new byte[]{1, 2, 3});

        UploadFileVo result = storage.storeAvatar(42L, image);

        assertTrue(result.getUrl().startsWith("/uploads/avatars/42/"));
        assertTrue(Files.exists(uploadRoot.resolve(result.getUrl().replace("/uploads/", ""))));
    }

    @Test
    void storesPostImageInTheCurrentUsersDirectory() throws Exception {
        FileStorageService storage = new FileStorageService(
                uploadRoot.toString(), "/uploads", 1024 * 1024);
        storage.initialize();
        MockMultipartFile image = new MockMultipartFile(
                "file", "reading.png", "image/png", new byte[]{1, 2, 3});

        UploadFileVo result = storage.storePostImage(42L, image);

        assertTrue(result.getUrl().startsWith("/uploads/posts/42/"));
        assertTrue(Files.exists(uploadRoot.resolve(result.getUrl().replace("/uploads/", ""))));
    }

    @Test
    void rejectsUnsupportedAvatarContentType() {
        FileStorageService storage = new FileStorageService(
                uploadRoot.toString(), "/uploads", 1024 * 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file", "portrait.txt", "text/plain", new byte[]{1});

        assertThrows(BusinessException.class, () -> storage.storeAvatar(42L, file));
    }
}
