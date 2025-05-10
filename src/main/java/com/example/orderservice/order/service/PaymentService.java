package com.example.orderservice.payment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WebClient webClient = WebClient.create("https://api.iamport.kr");

    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    // 1) merchant_uid 로 포트원에서 imp_uid, amount 등을 조회
    public Mono<JsonNode> getPaymentInfo(String merchantUid) {
        return getToken()
                .flatMap(token ->
                        webClient.get()
                                .uri("/payments/find/{merchant_uid}", merchantUid)
                                .header("Authorization", "Bearer " + token)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .map(json -> json.get("response"))
                );
    }

    // 2) imp_uid, amount, reason, checksum 으로 실제 환불 요청 */
    public Mono<CancelPaymentResponseDTO> cancelPayment(String impUid,
                                                        int amount,
                                                        String reason,
                                                        int checksum) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("imp_uid",  impUid);
        body.put("amount",   amount);
        body.put("checksum", checksum);
        if (reason != null && !reason.isBlank()) {
            body.put("reason", reason);
        }

        return getToken()
                .flatMap(token ->
                        webClient.post()
                                .uri("/payments/cancel")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                // ① 상태코드가 2xx 가 아니면 아래 람다로
                                .retrieve()
                                .onStatus(status -> !status.is2xxSuccessful(), resp ->
                                        resp.bodyToMono(JsonNode.class)
                                                .flatMap(err -> Mono.error(
                                                        new RuntimeException(err.get("message").asText()))
                                                )
                                )
                                .bodyToMono(JsonNode.class)
                                .map(json -> {
                                    boolean ok = json.get("code").asInt() == 0;
                                    String  msg= ok ? "결제 취소 성공" : json.get("message").asText();
                                    return CancelPaymentResponseDTO.builder()
                                            .success(ok)
                                            .message(msg)
                                            .build();
                                })
                                .onErrorResume(ex ->
                                        Mono.just(CancelPaymentResponseDTO.builder()
                                                .success(false)
                                                .message("결제 취소 연동 실패: " + ex.getMessage())
                                                .build()
                                        )
                                )
                );
    }


    private Mono<String> getToken() {
        return webClient.post()
                .uri("/users/getToken")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "imp_key",    apiKey,
                        "imp_secret", apiSecret
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(j -> j.get("response").get("access_token").asText());
    }
}