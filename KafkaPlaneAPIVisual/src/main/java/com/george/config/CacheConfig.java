package com.george.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("flights", "flightsByAirline");
        cacheManager.setAsyncCacheMode(true); // Enable async caching for the reactive support
        cacheManager.setCaffeine(caffeineConfig());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
            .maximumSize(500) // Max 500 entries
            .expireAfterWrite(5, TimeUnit.MINUTES); // Expires after five minutes
    }
}