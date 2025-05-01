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

//    @Bean
//    public Consumer<OrderCreatedMessage> statusChange() {
//        return message -> {
//            log.info("Order Message 수신: {}", message);
//
//            // 다음 단계 바인딩 계산
//            String bindingName = switch (message.status()) {
//                case PAYMENT_COMPLETED   -> "statusChange-out-0";
//                case ORDER_CONFIRMED   -> "statusChange-out-1";
//                case ORDER_COOKING     -> "statusChange-out-2";
//                case ORDER_DELIVERING  -> "statusChange-out-3";
//                default -> null;
//            };
//
//            // 롤백(이전 단계) 바인딩 계산
//            String rollbackBinding = switch (message.status()) {
//                case PAYMENT_COMPLETED  -> "orderCreated-out-0";
//                case ORDER_CONFIRMED   -> "statusChange-out-0";
//                case ORDER_COOKING     -> "statusChange-out-1";
//                case ORDER_DELIVERING  -> "statusChange-out-2";
//                default -> null;
//            };
//
//            if (bindingName == null) {
//                log.warn("지원하지 않는 상태입니다: {}", message.status());
//                return;
//            }
//
//            orderService.saveOrderFromMessage(message)
//                    // 저장 성공 시 다음 단계로 발행
//                    .flatMap(unused -> {
//                        sendMessage(bindingName, message);
//                        return Mono.empty();
//                    })
//                    // 저장 실패 시 롤백 단계로 재발행
//                    .onErrorResume(error -> {
//                        log.error("메시지 처리 실패, 롤백 시도:", error);
//
//                        // 낙관적 락 실패나 ID 문제는 재발행하면 안됨 → 무한루프 유발
//                        if (error instanceof org.springframework.dao.OptimisticLockingFailureException ||
//                                error instanceof IllegalStateException) {
//                            log.warn("치명적인 상태 충돌로 재처리 중단: {}", error.getMessage());
//                            return Mono.empty(); // 재발행 안 함
//                        }
//
//                        if (rollbackBinding != null) {
//                            sendMessage(rollbackBinding, message);
//                        }
//                        return Mono.empty();
//                    })
//
//                    .subscribe();
//        };
//    }

    @Bean
    public Consumer<OrderCreatedMessage> statusChange() {
        return message -> {
            log.info("Order Message 수신: {}", message);

            String bindingName = switch (message.status()) {
                case PAYMENT_COMPLETED   -> "statusChange-out-0";
                case ORDER_CONFIRMED     -> "statusChange-out-1";
                case ORDER_COOKING       -> "statusChange-out-2";
                case ORDER_DELIVERING    -> "statusChange-out-3";
                default -> null;
            };

            String rollbackBinding = switch (message.status()) {
                case PAYMENT_COMPLETED   -> "orderCreated-out-0";
                case ORDER_CONFIRMED     -> "statusChange-out-0";
                case ORDER_COOKING       -> "statusChange-out-1";
                case ORDER_DELIVERING    -> "statusChange-out-2";
                default -> null;
            };

            if (bindingName == null) {
                log.warn("지원하지 않는 상태입니다: {}", message.status());
                return;
            }

            // 업데이트 혹은 저장 처리
            orderService.updateOrderFromMessage(message)
                    .then(Mono.fromRunnable(() -> sendMessage(bindingName, message)))
                    .onErrorResume(error -> {
                        log.error("메시지 처리 실패, 롤백 시도:", error);

                        if (error instanceof org.springframework.dao.OptimisticLockingFailureException ||
                                error instanceof IllegalStateException) {
                            log.warn("치명적인 상태 충돌로 재처리 중단: {}", error.getMessage());
                            return Mono.empty();
                        }

                        if (rollbackBinding != null) {
                            sendMessage(rollbackBinding, message);
                        }
                        return Mono.empty();
                    })
                    .subscribe();
        };
    }


    // StreamBridge 로직을 중복 없이 처리하기 위한 헬퍼
    private void sendMessage(String destination, OrderCreatedMessage msg) {
        boolean sent = streamBridge.send(destination,
                MessageBuilder.withPayload(msg).build()
        );
        if (!sent) {
            log.error("MQ 발행 실패: {}", destination);
        } else {
            log.info("MQ 발행 성공: {}", destination);
        }
    }
}
