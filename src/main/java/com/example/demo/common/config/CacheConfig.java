package com.example.demo.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * CacheConfig 公共组件，提供后端各模块共享的基础能力。
 */
@Configuration
public class CacheConfig {

    // ==================== 公共方法 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(false);
        cacheManager.registerCustomCache(
                "captcha",
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );
        return cacheManager;
    }
}
