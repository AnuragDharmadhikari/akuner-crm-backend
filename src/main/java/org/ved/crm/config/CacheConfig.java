package org.ved.crm.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        // Map.ofEntries used instead of Map.of()
        // Map.of() has a hard limit of 10 entries
        // Map.ofEntries has no limit
        Map<String, RedisCacheConfiguration> cacheConfigurations =
                Map.ofEntries(

                        // ── Analytics Caches (4) ───────────────────────────────────────
                        Map.entry("analytics::revenue",
                                defaultConfig.entryTtl(Duration.ofHours(1))),

                        Map.entry("analytics::gst",
                                defaultConfig.entryTtl(Duration.ofHours(1))),

                        Map.entry("analytics::outstanding",
                                defaultConfig.entryTtl(Duration.ofMinutes(30))),

                        Map.entry("analytics::reps",
                                defaultConfig.entryTtl(Duration.ofMinutes(30))),

                        // ── AI Feature Caches (6) ──────────────────────────────────────
                        // Doctor engagement score — stable, 24h per doctor
                        Map.entry("ai::doctor-engagement",
                                defaultConfig.entryTtl(Duration.ofHours(24))),

                        // Pre-visit briefing — 1h per visit
                        Map.entry("ai::visit-briefing",
                                defaultConfig.entryTtl(Duration.ofHours(1))),

                        // Stockist payment risk — 6h per stockist
                        Map.entry("ai::payment-risk",
                                defaultConfig.entryTtl(Duration.ofHours(6))),

                        // Chemist payment risk — 6h per chemist
                        Map.entry("ai::chemist-payment-risk",
                                defaultConfig.entryTtl(Duration.ofHours(6))),

                        // Territory narrative — 12h per territory
                        Map.entry("ai::territory-narrative",
                                defaultConfig.entryTtl(Duration.ofHours(12))),

                        // Order recommendation — 2h per chemist
                        Map.entry("ai::order-recommendation",
                                defaultConfig.entryTtl(Duration.ofHours(2)))
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}