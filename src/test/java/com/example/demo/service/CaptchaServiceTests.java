package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.config.CacheConfig;
import com.example.demo.vo.CaptchaVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaServiceTests {

    private org.springframework.cache.CacheManager cacheManager;
    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        cacheManager = new CacheConfig().cacheManager();
        captchaService = new CaptchaService(cacheManager);
    }

    @Test
    void issuesCaptchaWithIdAndBase64Png() {
        CaptchaVo result = captchaService.issue();

        assertNotNull(result.getCaptchaId());
        assertFalse(result.getCaptchaId().isBlank());
        assertTrue(result.getImageBase64().startsWith("iVBOR"));
        assertTrue(result.getExpiresInSeconds() > 0);
    }

    @Test
    void issuesCaptchaWithFiveMinuteLifetime() {
        assertEquals(300, captchaService.issue().getExpiresInSeconds());
    }

    @Test
    void verifiesAndConsumesMatchingCodeOnlyOnce() {
        String captchaId = "captcha-1";
        putHash(captchaId, "AbCde");

        assertDoesNotThrow(() -> captchaService.verifyAndConsume(captchaId, "abcDE"));
        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume(captchaId, "abcDE")
        );
    }

    @Test
    void rejectsWrongMissingAndMalformedCaptcha() {
        putHash("captcha-2", "AbCde");

        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("captcha-2", "wrong")
        );
        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("missing", "abcde")
        );
        putHash("captcha-4", "AbCde");
        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("captcha-4", "")
        );
        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("captcha-4", "abcde")
        );
    }

    @Test
    void consumesCaptchaAfterWrongCodeAttempt() {
        putHash("captcha-3", "AbCde");

        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("captcha-3", "wrong")
        );
        assertThrows(
                BusinessException.class,
                () -> captchaService.verifyAndConsume("captcha-3", "abcde")
        );
    }

    private void putHash(String captchaId, String code) {
        Cache cache = cacheManager.getCache("captcha");
        assertNotNull(cache);
        cache.put(captchaId, sha256(code.toUpperCase()));
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
