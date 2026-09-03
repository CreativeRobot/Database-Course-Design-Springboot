package com.example.demo.common.config;

import com.example.demo.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

    // ==================== 公共方法 ====================

/**
 * 管理员权限拦截器，负责校验当前请求是否具备管理员身份。
 */
    /**
     * 在控制器执行前进行请求拦截和权限校验。
     */
@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Object role = request.getAttribute("role");
        if (Role.ADMIN.name().equals(role)) {
            return true;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"code\":403,\"message\":\"需要管理员权限\",\"data\":null}"
        );
        return false;
    }
}
