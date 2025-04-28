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

    // 배송 시작 이벤트 수신
    @Bean
    public Consumer<OrderDispatchedMessage> dispatchOrder() {
        return event -> {
            log.info("[dispatchOrder] 배송 시작 이벤트 수신: {}", event);

            // 배송 시작 후 주문 수락 메시지 발행
            AcceptOrderMessage acceptOrderMessage = new AcceptOrderMessage(
                    event.merchantUid(),
                    "ACCEPTED"
            );

            log.info("[dispatchOrder] 주문 수락 이벤트 발행: {}", acceptOrderMessage);
            streamBridge.send("acceptOrder-out-0", MessageBuilder.withPayload(acceptOrderMessage).build());
        };
    }
}
