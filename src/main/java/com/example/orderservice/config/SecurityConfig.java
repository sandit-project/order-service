package com.example.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.shared-secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // CSRF 전체 비활성화
                .csrf(csrf -> csrf.disable())

                .authorizeExchange(ex -> ex
                        // 필요한 경로 허용
                        .pathMatchers("/orders/prepare", "/orders/update-success", "/orders/update-fail").permitAll()
                        // 그 외 모든 요청: ADMIN 이면 무조건 ok, 아니면 인증된 사용자만
                        .anyExchange().access((monoAuth, ctx) ->
                                monoAuth
                                        // monoAuth: Authentication
                                        .map(auth -> {
                                            boolean isAdmin = auth.getAuthorities().stream()
                                                    .anyMatch(grant -> grant.getAuthority().equals("ROLE_ADMIN"));
                                            return new AuthorizationDecision(isAdmin || auth.isAuthenticated());
                                        })
                        )
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // Base64 문자열을 실제 키 바이트로 디코딩
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA512");
        return NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
