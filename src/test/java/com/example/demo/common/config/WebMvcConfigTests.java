package com.example.demo.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebMvcConfigTests {

    @Test
    void allowsConfiguredOriginsToReadUploadedImages() {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(
                config,
                "allowedOrigins",
                "http://localhost:5173,http://localhost:3000"
        );
        ExposedCorsRegistry registry = new ExposedCorsRegistry();

        config.addCorsMappings(registry);

        CorsConfiguration uploadCors = registry.configurations().get("/uploads/**");
        assertNotNull(uploadCors);
        assertEquals(
                java.util.List.of("http://localhost:5173", "http://localhost:3000"),
                uploadCors.getAllowedOriginPatterns()
        );
        assertEquals(java.util.List.of("GET"), uploadCors.getAllowedMethods());
        assertEquals(
                "http://localhost:5173",
                uploadCors.checkOrigin("http://localhost:5173")
        );
    }

    private static class ExposedCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
