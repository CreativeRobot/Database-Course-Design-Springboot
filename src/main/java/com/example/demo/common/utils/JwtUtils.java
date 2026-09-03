package com.example.demo.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtils 公共组件，提供后端各模块共享的基础能力。
 */
@Component
public class JwtUtils {
    private final Key secretKey;
    private final long expirationTime;

    public JwtUtils(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.expiration:86400000}") long expirationTime) {
        byte[] secretBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret 至少需要32个字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationTime = expirationTime;
    }

    // ==================== 公共方法 ====================

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseToken(token).getExpiration();
            return expiration == null || expiration.before(new Date());
        } catch (Exception exception) {
            return true;
        }
    }
}