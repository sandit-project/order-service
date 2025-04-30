package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStreamListener {

    private final OrderService orderService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<OrderCreatedMessage> statusChange() {
        return message -> {
            log.info("Order Message 수신: {}", message);

            String bindingName = switch (message.status()) {
                case PAYMENT_COMPLETED -> "orderCreated-out-1";
                case ORDER_CONFIRMED   -> "orderCreated-out-2";
                case ORDER_CANCELLED   -> "orderCreated-out-3";
                case ORDER_COOKING     -> "orderCreated-out-4";
                case ORDER_DELIVERING  -> "orderCreated-out-5";
                case ORDER_DELIVERED   -> "orderCreated-out-6";
                case ORDER_CREATED     -> "orderCreated-out-0";
                default -> null;
            };

            if (bindingName == null) {
                log.warn("지원하지 않는 상태입니다: {}", message.status());
                return;
            }

            orderService.saveOrderFromMessage(message)
                    .doOnSuccess(unused -> {
                        boolean result = streamBridge.send(bindingName, MessageBuilder.withPayload(message).build());

                        if (!result) {
                            log.error("MQ 발행 실패: {}", bindingName);
                        } else {
                            log.info("MQ 발행 성공: {}", bindingName);
                        }
                    })
                    .doOnError(error -> log.error("메시지 처리 실패", error))
                    .subscribe();
        };
    }
}
