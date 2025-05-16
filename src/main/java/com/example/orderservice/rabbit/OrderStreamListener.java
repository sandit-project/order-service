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

    // 상태 변경 메시지 수신 처리 + 실패 시 롤백
    @Bean
    public Consumer<OrderCreatedMessage> statusChange() {
        return message -> {
            log.info("[statusChange] 상태 메시지 수신: {}", message);

            orderService.getOrderByMerchantUid(message.merchantUid())
                    .collectList()
                    .flatMap(orders -> {
                        if (orders.isEmpty()) {
                            log.warn("해당 merchantUid 주문 없음: {}", message.merchantUid());
                            return Mono.error(new IllegalStateException("해당 merchantUid 주문 없음: " + message.merchantUid()));                        }
                        OrderStatus currentStatus = orders.get(0).getStatus();
                        OrderStatus targetStatus = message.status();
                        if (currentStatus == targetStatus) {
                            log.info("[statusChange] 상태 동일 → 처리 생략");
                            return Mono.empty();
                        }
                        // 상태 변경은 항상 수행
                        Mono<Void> statusChangeMono = orderService.changeOrderStatus(message.merchantUid(), targetStatus);
                        // ORDER_COOKING일 때만 큐로 메시지 전송
                        if (targetStatus == OrderStatus.ORDER_COOKING) {
                            return statusChangeMono.then(sendToQueue("statusChange-out-0", message));
                        } else {
                            log.info("[statusChange] status=ORDER_COOKING이 아니므로 큐 전송 생략");
                            return statusChangeMono;
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("[statusChange] 상태 처리 실패 → 롤백 진행: {}", e);
                        OrderStatus rollbackStatus = determineRollbackStatus(message.status());
                        if (rollbackStatus == null) {
                            log.warn("[statusChange] 롤백 대상 아님 → 종료");
                            return Mono.empty();
                        }
                        // 롤백 처리 후 롤백 큐로 메시지 발행
                        return orderService.updateStatusWithRollback(message.merchantUid(), rollbackStatus, message.status())
                                .then(sendToQueue("orderRollback-out-0",
                                        OrderCreatedMessage.builder()
                                                .merchantUid(message.merchantUid())
                                                .status(rollbackStatus)
                                                .build()
                                ));
                    })
                    .subscribe();
        };
    }

    // 상태 변경 실패 시 롤백 대상 상태 계산 (e.g., delivered → delivering)
    private OrderStatus determineRollbackStatus(OrderStatus failedStatus) {
        return switch (failedStatus) {
            case ORDER_DELIVERED -> OrderStatus.ORDER_DELIVERING;
            case ORDER_DELIVERING -> OrderStatus.ORDER_COOKING;
            default -> null;
        };
    }

    // 특정 큐(destination)로 메시지를 전송
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
