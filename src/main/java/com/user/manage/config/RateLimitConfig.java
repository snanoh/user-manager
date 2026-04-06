package com.user.manage.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.capacity:20}")
    private long capacity;

    @Value("${rate-limit.refill-period-seconds:60}")
    private long refillPeriodSeconds;

    @Bean
    public Map<String, Bucket> ipBucketCache() {
        return new ConcurrentHashMap<>();
    }

    public Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofSeconds(refillPeriodSeconds))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
