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

            orderService.getOrderByMerchantUid(message.merchantUid())
                    .collectList()
                    .flatMap(existingOrders -> {
                        if (existingOrders.isEmpty()) {
                            log.warn("[statusChange] 해당 주문 없음: {}", message.merchantUid());
                            return Mono.empty();
                        }

                        
                        // 상태를 바꾸기 전에 갖고 있는 기존 상태
                        OrderStatus previousStatus = existingOrders.get(0).getStatus();
                        boolean needUpdate = existingOrders.stream()
                                .anyMatch(order -> order.getStatus() != message.status());

                        if (!needUpdate) {
                            log.info("[statusChange] 상태 동일 → 처리 생략");
                            return Mono.empty();
                        }

                        return orderService.changeOrderStatus(message.merchantUid(), message.status())
                                .flatMap(result -> {
                                    log.info("[statusChange] 상태 변경 완료: {}", result);

                                    if (message.status() == OrderStatus.ORDER_COOKING) {
                                        return sendToQueue("statusChange-out-3", message); // 딜리버리 큐 전송
                                    }

                                    return Mono.empty();
                                })
                        .onErrorResume(e -> {
                        log.error("[statusChange] 상태 처리 실패, 롤백 시도: {}", e.getMessage(), e);

                            OrderCreatedMessage rollbackMessage = OrderCreatedMessage.builder()
                                    .status(previousStatus)
                                    .merchantUid(message.merchantUid())
                                    .build();

                            String rollbackBinding = switch (previousStatus) {
                                case ORDER_CONFIRMED -> "statusChange-out-1";
                                case ORDER_COOKING -> "statusChange-out-2";
                                default -> null;
                            };

                            if (rollbackBinding != null) {
                                return sendToQueue(rollbackBinding, rollbackMessage)
                                        .then(Mono.empty());
                            }

                            return Mono.empty();
                        });
                    })
                    .subscribe(
                            null,
                            e -> log.error("[statusChange] 전체 처리 중 예외 발생: {}", e.getMessage(), e)
                    );
        };
    }


    private Mono<Void> sendToQueue(String destination, OrderCreatedMessage message) {
        return Mono.fromCallable(() -> {
            log.info("→ 큐 전송 시도: {} (status={})", destination, message.status());
            boolean sent = streamBridge.send(destination, MessageBuilder.withPayload(message).build());
            if (sent) {
                log.info("큐 전송 성공: {}", destination);
            } else {
                throw new IllegalStateException("큐 전송 실패: " + destination);
            }
            return null;
        });
    }
}
