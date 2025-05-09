package com.example.orderservice.payment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WebClient webClient = WebClient.create("https://api.iamport.kr");

    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    public Mono<CancelPaymentResponseDTO> cancelPayment(String merchantUid) {
        return getToken()
                .flatMap(token ->
                        webClient.post()
                                .uri("/payments/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .bodyValue(Map.of("merchant_uid", merchantUid))
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .map(json -> {
                                    boolean success = json.get("code").asInt() == 0;
                                    String message = success ? "결제 취소 성공" : json.get("message").asText();
                                    return CancelPaymentResponseDTO.builder()
                                            .success(success)
                                            .message(message)
                                            .build();
                                })
                );
    }

    private Mono<String> getToken() {
        return webClient.post()
                .uri("/users/getToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "imp_key", apiKey,
                        "imp_secret", apiSecret
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("response").get("access_token").asText());
    }
}