package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.Order;
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
            log.info("[statusChange] 상태 메시지 수신: {}", message);

            orderService.updateOrderFromMessage(message)
                    .onErrorResume(error -> {
                        log.error("[statusChange] 상태 처리 실패, 롤백 시도: {}", error.getMessage(), error);

                        String rollbackBinding = switch (message.status()) {
                            case ORDER_CONFIRMED -> "statusChange-out-0";
                            case ORDER_COOKING -> "statusChange-out-1";
                            default -> null;
                        };

                        if (rollbackBinding != null) {
                            return sendToQueue(rollbackBinding, message);
                        }

                        return Mono.empty();
                    })
                    .then(sendToQueueByStatus(message)) // <- 항상 큐 발송 시도
                    .subscribe();
        };
    }

    private Mono<Void> sendToQueueByStatus(OrderCreatedMessage message) {
        String nextQueue = switch (message.status()) {
            case PAYMENT_COMPLETED -> "statusChange-out-0";
            case ORDER_CONFIRMED   -> "statusChange-out-1";
            case ORDER_COOKING     -> "statusChange-out-2";
            case ORDER_DELIVERING  -> "statusChange-out-3";
            default -> null;
        };

        if (nextQueue != null) {
            return sendToQueue(nextQueue, message);
        } else {
            log.info("[sendToQueueByStatus] 상태 {}는 전파 없음", message.status());
            return Mono.empty();
        }
    }


    private Mono<Void> sendToQueue(String destination, OrderCreatedMessage message) {
        return Mono.fromRunnable(() -> {
            log.info("→ 큐 전송 시도: {} (status={})", destination, message.status());
            boolean sent = streamBridge.send(destination, MessageBuilder.withPayload(message).build());
            if (sent) {
                log.info("큐 전송 성공: {}", destination);
            } else {
                log.error("큐 전송 실패: {}", destination);
            }
        });
    }
}
