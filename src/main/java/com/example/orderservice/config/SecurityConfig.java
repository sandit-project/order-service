package com.example.orderservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.shared-secret}")
    private String jwtSecret;

//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http
//                // CSRF 전체 비활성화
//                .csrf(csrf -> csrf.disable())
//
//                .authorizeExchange(ex -> ex
//                        // 필요한 경로 허용
//                        .pathMatchers("/orders/prepare", "/orders/update-success", "/orders/update-fail").permitAll()
//                        // 그 외 모든 요청: ADMIN 이면 무조건 ok, 아니면 인증된 사용자만
//                        .anyExchange().access((monoAuth, ctx) ->
//                                monoAuth
//                                        // monoAuth: Authentication
//                                        .map(auth -> {
//                                            boolean isAdmin = auth.getAuthorities().stream()
//                                                    .anyMatch(grant -> grant.getAuthority().equals("ROLE_ADMIN"));
//                                            return new AuthorizationDecision(isAdmin || auth.isAuthenticated());
//                                        })
//                        )
//                )
//
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(Customizer.withDefaults())
//                );
//        return http.build();
//    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter socialAuthFilter = new AuthenticationWebFilter(new SocialAuthenticationManager());
        socialAuthFilter.setServerAuthenticationConverter(new SocialTokenConverter());

        socialAuthFilter.setRequiresAuthenticationMatcher(exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            System.out.println("token: " + token);
            if (token != null && token.startsWith("Bearer ")
                    && (token.contains("naver:") || token.contains("kakao:") || token.contains("google:"))) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        });

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/orders/prepare", "/orders/update-success", "/orders/update-fail").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(socialAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

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

    // ===== 소셜 인증 컨버터 =====
    static class SocialTokenConverter implements ServerAuthenticationConverter {
        @Override
        public Mono<Authentication> convert(ServerWebExchange exchange) {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (token.startsWith("naver:") || token.startsWith("kakao:") || token.startsWith("google:")) {
                    return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
                }
            }
            return Mono.empty();
        }
    }

    // ===== 소셜 인증 매니저 =====
    static class SocialAuthenticationManager implements ReactiveAuthenticationManager {
        private final WebClient webClient = WebClient.create();

        @Override
        public Mono<Authentication> authenticate(Authentication authentication) {
            String fullToken = (String) authentication.getCredentials();
            System.out.println("Social Token Received: " + fullToken);

            String[] parts = fullToken.split(":", 3);
            if (parts.length < 3) {
                System.out.println("Invalid token format");
                return Mono.error(new BadCredentialsException("Invalid social token format"));
            }

            String provider = parts[0];
            String accessToken = parts[2];

            if ("naver".equals(provider)) {
                System.out.println("Validating Naver token...");
                return webClient.get()
                        .uri("https://openapi.naver.com/v1/nid/me")
                        .headers(h -> h.setBearerAuth(accessToken))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .doOnNext(body -> System.out.println("Naver API Response: " + body))
                        .handle((body, sink) -> {
                            if (!"00".equals(body.path("resultcode").asText())) {
                                sink.error(new BadCredentialsException("Invalid Naver token"));
                                return;
                            }
                            String userId = body.path("response").path("id").asText();
                            var auth = new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            System.out.println("Naver user authenticated: " + userId);
                            sink.next(auth);
                        });
            }

            return Mono.error(new BadCredentialsException("Unsupported provider"));
        }
    }
}
