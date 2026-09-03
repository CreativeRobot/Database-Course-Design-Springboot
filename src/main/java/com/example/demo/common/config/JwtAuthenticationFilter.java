package com.example.demo.common.config;

import com.example.demo.common.utils.JwtUtils;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * JWT 认证过滤器，负责从请求中解析令牌并建立当前用户身份。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public JwtAuthenticationFilter(
            JwtUtils jwtUtils,
            UserRepository userRepository,
            SecurityErrorResponseWriter securityErrorResponseWriter) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    // ==================== 公共方法 ====================

    /**
     * 处理当前 HTTP 请求，完成身份校验后继续执行过滤器链。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
            return;
        }

        try {
            Claims claims = jwtUtils.parseToken(authHeader.substring(7).trim());
            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
                return;
            }

            Long userId = parseUserId(claims.get("userId"));
            if (userId == null) {
                reject(response, HttpStatus.UNAUTHORIZED, "Token用户信息无效");
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                reject(response, HttpStatus.UNAUTHORIZED, "用户不存在");
                return;
            }
            if (!Integer.valueOf(1).equals(user.getStatus())) {
                reject(response, HttpStatus.FORBIDDEN, "账号已被禁用");
                return;
            }
            if (user.getRole() == null) {
                reject(response, HttpStatus.FORBIDDEN, "用户角色无效");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.setAttribute("user", user);
            request.setAttribute("userId", user.getId());
            request.setAttribute("username", user.getUsername());
            request.setAttribute("role", user.getRole().name());
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            reject(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("/api/auth/login".equals(path)
                || "/api/auth/register".equals(path)
                || "/api/auth/captcha".equals(path)
                || "/api/auth/forgot-password".equals(path)) {
            return true;
        }
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        return "/api/auth/security-questions".equals(path)
                || isPathOrChild(path, "/api/books")
                || isPathOrChild(path, "/api/categories")
                || isPathOrChild(path, "/api/authors")
                || isPathOrChild(path, "/api/publishers")
                || isPathOrChild(path, "/api/community/posts")
                || isPathOrChild(path, "/uploads")
                || "/api/recommendations/home".equals(path);
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private boolean isPathOrChild(String path, String publicPath) {
        return publicPath.equals(path) || path.startsWith(publicPath + "/");
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private Long parseUserId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            try {
                return Long.valueOf(string);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    private void reject(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        securityErrorResponseWriter.write(response, status, message);
    }
}
