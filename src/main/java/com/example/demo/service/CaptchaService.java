package com.example.demo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.vo.CaptchaVo;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * CaptchaService 业务服务，封装相关领域的业务规则和数据访问流程。
 */
@Service
public class CaptchaService {

    private static final String CACHE_NAME = "captcha";
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 5;
    private static final int IMAGE_WIDTH = 140;
    private static final int IMAGE_HEIGHT = 48;
    private static final int EXPIRES_IN_SECONDS = 300;
    private static final String ERROR_MESSAGE = "验证码错误或已过期";

    private final CacheManager cacheManager;
    private final SecureRandom random = new SecureRandom();

    public CaptchaService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // ==================== 业务方法 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    public CaptchaVo issue() {
        String code = randomCode();
        String captchaId = UUID.randomUUID().toString();
        cache().put(captchaId, sha256(normalize(code)));
        return new CaptchaVo(captchaId, renderBase64(code), EXPIRES_IN_SECONDS);
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    public void verifyAndConsume(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId)
                || captchaId.length() > 64) {
            throw invalidCaptcha();
        }

        Cache<String, byte[]> cache = cache();
        byte[] expectedHash = cache.getIfPresent(captchaId);
        if (expectedHash == null) {
            throw invalidCaptcha();
        }

        boolean validCode = StringUtils.hasText(captchaCode) && captchaCode.length() <= 16;
        byte[] actualHash = validCode
                ? sha256(normalize(captchaCode))
                : new byte[0];
        boolean matches = validCode && MessageDigest.isEqual(expectedHash, actualHash);
        boolean consumed = cache.asMap().remove(captchaId, expectedHash);
        if (!matches || !consumed) {
            throw invalidCaptcha();
        }
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @SuppressWarnings("unchecked")
    private Cache<String, byte[]> cache() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(CACHE_NAME);
        if (springCache == null) {
            throw new IllegalStateException("验证码缓存未配置");
        }
        return (Cache<String, byte[]>) springCache.getNativeCache();
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private String renderBase64(String code) {
        BufferedImage image = new BufferedImage(
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics.setColor(new Color(248, 248, 244));
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

            graphics.setStroke(new BasicStroke(1.2f));
            for (int index = 0; index < 4; index++) {
                graphics.setColor(new Color(150, 165, 180, 150));
                graphics.drawLine(
                        random.nextInt(IMAGE_WIDTH),
                        random.nextInt(IMAGE_HEIGHT),
                        random.nextInt(IMAGE_WIDTH),
                        random.nextInt(IMAGE_HEIGHT)
                );
            }
            for (int index = 0; index < 70; index++) {
                graphics.setColor(new Color(120, 130, 140, 110));
                int x = random.nextInt(IMAGE_WIDTH);
                int y = random.nextInt(IMAGE_HEIGHT);
                graphics.fillRect(x, y, 1, 1);
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            Color[] colors = {
                    new Color(40, 83, 120),
                    new Color(146, 67, 61),
                    new Color(47, 112, 94),
                    new Color(104, 76, 137),
                    new Color(161, 98, 39)
            };
            for (int index = 0; index < code.length(); index++) {
                int x = 14 + index * 25;
                int y = 34 + random.nextInt(5);
                AffineTransform original = graphics.getTransform();
                graphics.rotate(
                        Math.toRadians(random.nextInt(25) - 12),
                        x + 10,
                        y - 10
                );
                graphics.setColor(colors[index % colors.length]);
                graphics.drawString(String.valueOf(code.charAt(index)), x, y);
                graphics.setTransform(original);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("验证码图片生成失败", exception);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256不可用", exception);
        }
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private BusinessException invalidCaptcha() {
        return new BusinessException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
    }
}
