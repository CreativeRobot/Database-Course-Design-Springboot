package com.example.demo.common.config;

import com.example.demo.common.utils.JwtUtils;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicGetRequest(request)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }

        Claims claims;
        try {
            claims = jwtUtils.parseToken(authHeader.substring(7).trim());
        } catch (Exception exception) {
            return reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }

        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            return reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }

        Long userId = parseUserId(claims.get("userId"));
        if (userId == null) {
            return reject(response, HttpStatus.UNAUTHORIZED, "Token用户信息无效");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return reject(response, HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            return reject(response, HttpStatus.FORBIDDEN, "账号已被禁用");
        }
        if (user.getRole() == null) {
            return reject(response, HttpStatus.FORBIDDEN, "用户角色无效");
        }

        request.setAttribute("user", user);
        request.setAttribute("userId", user.getId());
        request.setAttribute("username", user.getUsername());
        request.setAttribute("role", user.getRole().name());
        return true;
    }

    private boolean isPublicGetRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        return "/api/auth/captcha".equals(path)
                || isPathOrChild(path, "/api/books")
                || isPathOrChild(path, "/api/categories")
                || isPathOrChild(path, "/api/authors")
                || isPathOrChild(path, "/api/publishers");
    }

    private boolean isPathOrChild(String path, String publicPath) {
        return publicPath.equals(path) || path.startsWith(publicPath + "/");
    }

    private Long parseUserId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            try {
                return Long.valueOf(string);
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }

    private boolean reject(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        securityErrorResponseWriter.write(response, status, message);
        return false;
    }
}