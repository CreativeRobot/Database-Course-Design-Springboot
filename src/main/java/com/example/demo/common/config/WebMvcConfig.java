package com.example.demo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类，负责跨域、拦截器和资源映射等 Web 层配置。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ==================== 公共方法 ====================

    /**
     * 创建并保存当前业务数据。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = StringUtils.commaDelimitedListToStringArray(allowedOrigins);
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
        registry.addMapping("/uploads/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * 创建并保存当前业务数据。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
