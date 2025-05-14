package com.example.orderservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.username}")
    private String username;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        String redisUrl = "redis://" + host + ":" + port;

        config.useSingleServer()
                .setAddress(redisUrl);

        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }

        // 대부분의 Redis는 username 미사용이므로 실제 설정에 따라 판단 필요
        config.useSingleServer().setUsername(username); // 필요 시만 활성화

        return Redisson.create(config);
    }
}