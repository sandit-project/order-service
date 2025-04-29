package com.example.orderservice.rabbit;

import com.example.orderservice.event.AcceptOrderMessage;
import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.event.OrderDispatchedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
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
            log.info("[orderCreated] 주문 생성 이벤트 발행: {}", event);
            return MessageBuilder.withPayload(event).build();
        };
    }
}
