package com.autumnus.spring_boot_starter_template.common.rate_limiting;

import com.autumnus.spring_boot_starter_template.common.config.RateLimitingProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitingProperties properties;

    public RateLimitingService(RateLimitingProperties properties) {
        this.properties = properties;
    }

    public boolean tryConsume(String key) {
        return resolveBucket(key).tryConsume(1);
    }

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        final Bandwidth limit = Bandwidth.classic(
                properties.getCapacity(),
                Refill.intervally(properties.getCapacity(), properties.getRefillPeriod()));
        return Bucket.builder().addLimit(limit).build();
    }
}
