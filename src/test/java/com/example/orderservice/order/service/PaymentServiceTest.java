package com.example.orderservice.order.service;
import com.example.orderservice.payment.CancelPaymentResponseDTO;
import com.example.orderservice.payment.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private WebClient webClient;
    @Mock
    private RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;
    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RequestBodySpec requestBodySpec;
    @Mock
    private ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "webClient", webClient);

        // ① getToken() 내부의 WebClient.post() 흐름을 스텁
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/users/getToken")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // ② 토큰 JSON 응답도 스텁
        JsonNode tokenJson = Mockito.mock(JsonNode.class);
        JsonNode responseNode = Mockito.mock(JsonNode.class);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(tokenJson));
        when(tokenJson.get("response")).thenReturn(responseNode);
        when(responseNode.get("access_token")).thenReturn(Mockito.mock(JsonNode.class));
        when(responseNode.get("access_token").asText()).thenReturn("token");
    }


    @Test
    void 결제정보_조회_성공() {
        when(paymentService.getToken()).thenReturn(Mono.just("token"));
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/payments/find/{merchant_uid}", "m1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header("Authorization", "Bearer token")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        JsonNode node = Mockito.mock(JsonNode.class);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(node));

        StepVerifier.create(paymentService.getPaymentInfo("m1"))
                .expectNext(node)
                .verifyComplete();
    }

    @Test
    void 결제취소_실패_에러발생() {
        when(paymentService.getToken()).thenReturn(Mono.just("token"));
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/payments/cancel")).thenReturn(requestBodySpec);
        when(requestBodySpec.header("Authorization", "Bearer token")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(
                new WebClientResponseException(400, "Bad Request", null, null, null)
        );

        StepVerifier.create(paymentService.cancelPayment("i1", 100, "r", 100))
                .expectNextMatches(resp -> !resp.isSuccess())
                .verifyComplete();
    }

    @Test
    void 결제취소_성공_코드0() {
        when(paymentService.getToken()).thenReturn(Mono.just("token"));
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/payments/cancel")).thenReturn(requestBodySpec);
        when(requestBodySpec.header("Authorization", "Bearer token")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        JsonNode json = Mockito.mock(JsonNode.class);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(json));
        JsonNode codeNode = Mockito.mock(JsonNode.class);
        when(json.get("code")).thenReturn(codeNode);
        when(codeNode.asInt()).thenReturn(0);

        StepVerifier.create(paymentService.cancelPayment("i1", 100, "r", 100))
                .expectNextMatches(CancelPaymentResponseDTO::isSuccess)
                .verifyComplete();
    }
}
