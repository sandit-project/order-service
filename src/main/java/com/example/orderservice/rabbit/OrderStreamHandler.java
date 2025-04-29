package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStreamHandler {

    private final StreamBridge streamBridge;

    // 주문 생성 이벤트 발행
    @Bean
    public Function<OrderCreatedMessage, Message<OrderCreatedMessage>> orderCreated() {
        return event -> {
            log.info("[orderCreated] 주문 생성 이벤트 수신: {}", event);

            switch (event.status()) {
                case PAYMENT_COMPLETED, ORDER_CONFIRMED, ORDER_CANCELLED, ORDER_COOKING, ORDER_DELIVERING, ORDER_DELIVERED -> {
                    log.info("[orderCreated] 등록 대상 상태, 이벤트 발행: {}", event);
                    return MessageBuilder.withPayload(event).build();
                }
                default -> {
                    log.warn("[orderCreated] 등록 대상 아님, 발행 스킵: {}", event.status());
                    return null; // 발행 안 함
                }
            }
        };
    }


}
