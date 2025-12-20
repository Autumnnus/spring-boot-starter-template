package com.autumnus.spring_boot_starter_template.common.idempotency;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class IdempotencyKeyValidator {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public IdempotencyKeyValidator(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to mark a key as used. Returns true if successful (key was not used before),
     * false if the key was already used.
     */
    public Mono<Boolean> tryMarkAsUsed(String key) {
        return tryMarkAsUsed(key, DEFAULT_TTL);
    }

    /**
     * Attempts to mark a key as used with custom TTL.
     */
    public Mono<Boolean> tryMarkAsUsed(String key, Duration ttl) {
        final String redisKey = KEY_PREFIX + key;
        return redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "used", ttl)
                .defaultIfEmpty(false);
    }

    /**
     * Checks if a key has been used before.
     */
    public Mono<Boolean> isKeyUsed(String key) {
        final String redisKey = KEY_PREFIX + key;
        return redisTemplate.hasKey(redisKey)
                .defaultIfEmpty(false);
    }
}
