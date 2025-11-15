package com.inspecthub.admin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test Cache Configuration
 *
 * Integration Test에서 Redis 없이도 캐시 동작을 테스트할 수 있도록
 * ConcurrentMapCacheManager 사용 (In-Memory)
 */
@TestConfiguration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // In-Memory Cache Manager (Redis 대신)
        return new ConcurrentMapCacheManager("system:login-policy");
    }
}
