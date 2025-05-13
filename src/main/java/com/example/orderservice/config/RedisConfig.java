package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = "stringReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        var ser = new StringRedisSerializer();
        var context = RedisSerializationContext.<String, String>newSerializationContext(ser)
                .key(ser)
                .value(ser)
                .hashKey(ser)
                .hashValue(ser)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
