package com.example.orderservice.order.domain;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class OrderCancelRedisRepository {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final ReactiveRedisTemplate<String, String> redis;

    public OrderCancelRedisRepository(
            @Qualifier("stringReactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis
    ) {
        this.redis = redis;
    }

    public Mono<Boolean> savePreviousState(String merchantUid, String status) {
        return redis.opsForValue()
                .set(merchantUid, status, STATE_TTL);
    }

    public Mono<String> fetchPreviousState(String merchantUid) {
        return redis.opsForValue().get(merchantUid);
    }

    public Mono<Boolean> deleteState(String merchantUid) {
        return redis.opsForValue().delete(merchantUid);
    }
}

