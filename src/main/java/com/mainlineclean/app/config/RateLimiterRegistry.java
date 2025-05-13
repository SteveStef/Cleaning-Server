package com.mainlineclean.app.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import org.springframework.stereotype.Component;
import java.time.Duration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class RateLimiterRegistry {
    private final Cache<String, Bucket> cache = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(1)).build();

    public Bucket resolveBucket(String userToken) {
        return cache.get(userToken, this::newBucket);
    }

    private Bucket newBucket(String userToken) { // 8 requests per second
        Bandwidth limit;
        if(userToken.endsWith("POST/review")) { // for creating review 2 per day per person
            limit = Bandwidth.builder().capacity(2).refillIntervally(2, Duration.ofDays(1)).build();
        } else if(userToken.endsWith("POST/requestQuote")) { // for sending emails 5 emails then 1 email every hour
            limit = Bandwidth.builder().capacity(5).refillIntervally(1, Duration.ofHours(1)).build();
        } else {
            // keep in mind this means 5 requests for the same route per second
            limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofSeconds(1)).build();
        }
        return Bucket.builder().addLimit(limit).build();
    }
}

// TODO
/*
    If you want to make this full proof, on the getToken route return the same token for same IP
*/