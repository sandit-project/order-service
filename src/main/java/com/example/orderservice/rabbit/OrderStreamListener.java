package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.DeliveryAddressRepository;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.model.DeliveryAddress;
import com.example.orderservice.order.service.OrderService;
import com.example.orderservice.payment.CancelPaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStreamListener {

    private final OrderService orderService;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<OrderCreatedMessage> statusChange() {
        return message -> {
            log.info("[statusChange] 상태 메시지 수신: {}", message);

            // 배송주소 저장 (OrderCreatedMessage 이벤트 처리 시)
            if (message.addressStart() != null && message.addressDestination() != null) {
                DeliveryAddress entity = new DeliveryAddress();
                entity.setMerchantUid(message.merchantUid());
                entity.setAddressStart(message.addressStart());
                entity.setAddressDestination(message.addressDestination());

                deliveryAddressRepository.save(entity)
                        .doOnSuccess(saved -> log.info("[statusChange] 배송주소 저장 완료: uid={}", saved.getUid()))
                        .onErrorResume(DataIntegrityViolationException.class, ex -> {
                            log.warn("[statusChange] 주소 저장 스킵(위도·경도 정보 없음): merchantUid={}", message.merchantUid(), ex);
                            return Mono.empty();
                        })
                        // 그 외 예외만 보상 트랜잭션으로 연결
                        .doOnError(err -> {
                            log.error("[statusChange] 배송주소 저장 중 치명적 오류, 보상 트랜잭션 수행", err);
                            orderService.cancelOrderPayment(message.merchantUid(), "배송주소 저장 실패 보상")
                                    .subscribe(
                                            resp -> log.info("[statusChange] 보상 결제취소 완료: {}", resp),
                                            cErr -> log.error("[statusChange] 보상 결제취소 실패", cErr)
                                    );
                        })
                        .subscribe();
            }

            // 주문 상태 변경 로직
            orderService.getOrderByMerchantUid(message.merchantUid())
                    .collectList()
                    .flatMap(existingOrders -> {
                        if (existingOrders.isEmpty()) {
                            log.warn("[statusChange] 해당 주문 없음: {}", message.merchantUid());
                            return Mono.empty();
                        }

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
                                        return sendToQueue("statusChange-out-2", message);
                                    }

                                    return Mono.empty();
                                })
                                .onErrorResume(e -> {
                                    log.error("[statusChange] 상태 처리 실패, 롤백 시도: {}", e.getMessage(), e);

                                    OrderCreatedMessage rollbackMessage = OrderCreatedMessage.builder()
                                            .merchantUid(message.merchantUid())
                                            .status(previousStatus)
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

//    @Bean
//    public Consumer<OrderCreatedMessage> statusChange() {
//        return message -> {
//            log.info("[statusChange] 상태 메시지 수신: {}", message);
//
//            orderService.getOrderByMerchantUid(message.merchantUid())
//                    .collectList()
//                    .flatMap(existingOrders -> {
//                        if (existingOrders.isEmpty()) {
//                            log.warn("[statusChange] 해당 주문 없음: {}", message.merchantUid());
//                            return Mono.empty();
//                        }
//
//
//                        // 상태를 바꾸기 전에 갖고 있는 기존 상태
//                        OrderStatus previousStatus = existingOrders.get(0).getStatus();
//                        boolean needUpdate = existingOrders.stream()
//                                .anyMatch(order -> order.getStatus() != message.status());
//
//                        if (!needUpdate) {
//                            log.info("[statusChange] 상태 동일 → 처리 생략");
//                            return Mono.empty();
//                        }
//
//                        return orderService.changeOrderStatus(message.merchantUid(), message.status())
//                                .flatMap(result -> {
//                                    log.info("[statusChange] 상태 변경 완료: {}", result);
//
//                                    if (message.status() == OrderStatus.ORDER_COOKING) {
//                                        return sendToQueue("statusChange-out-2", message); // 딜리버리 큐 전송
//                                    }
//
//                                    return Mono.empty();
//                                })
//                        .onErrorResume(e -> {
//                        log.error("[statusChange] 상태 처리 실패, 롤백 시도: {}", e.getMessage(), e);
//
//                            OrderCreatedMessage rollbackMessage = OrderCreatedMessage.builder()
//                                    .status(previousStatus)
//                                    .merchantUid(message.merchantUid())
//                                    .build();
//
//                            String rollbackBinding = switch (previousStatus) {
//                                case ORDER_CONFIRMED -> "statusChange-out-1";
//                                case ORDER_COOKING -> "statusChange-out-2";
//                                default -> null;
//                            };
//
//                            if (rollbackBinding != null) {
//                                return sendToQueue(rollbackBinding, rollbackMessage)
//                                        .then(Mono.empty());
//                            }
//
//                            return Mono.empty();
//                        });
//                    })
//                    .subscribe(
//                            null,
//                            e -> log.error("[statusChange] 전체 처리 중 예외 발생: {}", e.getMessage(), e)
//                    );
//        };
//    }


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
