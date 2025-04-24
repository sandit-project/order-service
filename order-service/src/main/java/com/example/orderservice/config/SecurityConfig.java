package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // CSRF 전체 비활성화
                .csrf(csrf -> csrf.disable())

                .authorizeExchange(ex -> ex
                        // 필요한 경로 허용
                        .pathMatchers("/orders/prepare", "/orders/update-success", "/orders/update-fail").permitAll()
                        .anyExchange().authenticated()
                )

                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
