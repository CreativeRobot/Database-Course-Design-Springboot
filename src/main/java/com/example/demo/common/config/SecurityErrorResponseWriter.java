package com.example.demo.common.config;

import com.example.demo.common.Result;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * SecurityErrorResponseWriter 公共组件，提供后端各模块共享的基础能力。
 */
@Component
public class SecurityErrorResponseWriter {
    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==================== 公共方法 ====================

    /**
     * 将安全错误信息写入 HTTP 响应。
     */
    public void write(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        if (status == HttpStatus.UNAUTHORIZED) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setHeader("Cache-Control", "no-store");
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), Result.error(status.value(), message));
    }
}

