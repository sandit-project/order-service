package com.example.orderservice.order.service;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.DeliveryAddress;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final TransactionalOperator txOp;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StreamBridge streamBridge;

    // 현재 시각을 반환하는 헬퍼 메서드 (테스트 시 오버라이드 용)
    protected LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    // 모든 주문 조회
    public Flux<Order> findAllOrders() {
        return orderRepository.findAllOrders();
    }

    // uid로 주문 조회 (단건)
    public Mono<Order> getOrderByUid(Integer uid) {
        return orderRepository.findById(uid);
    }

    // 유저 UID로 주문 전체 조회
    public Flux<Order> findAllByUserUid(Integer userUid) {
        return orderRepository.findByUserUid(userUid);
    }

    // MQ로 발행 가능한 주문 상태 검증 (6개 상태만 허용)
    private void validateStatusForQueue(OrderStatus status) {
        if (status != OrderStatus.PAYMENT_COMPLETED &&
                status != OrderStatus.ORDER_CONFIRMED &&
                status != OrderStatus.ORDER_CANCELLED &&
                status != OrderStatus.ORDER_COOKING &&
                status != OrderStatus.ORDER_DELIVERING &&
                status != OrderStatus.ORDER_DELIVERED) {
            throw new IllegalArgumentException("이 상태는 MQ에 발행할 수 없습니다: " + status);
        }
    }

    // 주문 저장 요청 처리 → DB 저장 후 상태 MQ 발행
    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
        log.info("[submitOrder] 요청 들어옴: merchantUid={}, version={}", dto.getMerchantUid(), dto.getVersion());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return Mono.error(new IllegalArgumentException("주문 항목이 없습니다."));
        }

        if (dto.getVersion() != 0) {
            log.warn("[submitOrder] 대표주문이므로 처리하지 않음: merchantUid={}", dto.getMerchantUid());
            return Mono.empty();
        }

        List<Order> ordersToSave = dto.getItems().stream()
                .filter(item -> {
                    boolean isRepresentative = item.menuName().contains("외") && item.amount() == 1;
                    boolean isIndividual = item.version() == 0 && !isRepresentative;
                    if (!isIndividual) {
                        log.warn("대표주문(item) 필터링됨: {}", item.menuName());
                    }
                    return isIndividual;
                })
                .map(item -> Order.builder()
                        .userUid(dto.getUserUid())
                        .socialUid(dto.getSocialUid())
                        .storeUid(dto.getStoreUid())
                        .merchantUid(dto.getMerchantUid())
                        .menuName(item.menuName())
                        .amount(item.amount())
                        .price(item.unitPrice())
                        .payment(dto.getPayment())
                        .status(dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED)
                        .createdDate(getNow())
                        .calorie(item.calorie())
                        .reservationDate(dto.getReservationDate())
                        .build()
                )
                .toList();

        DeliveryAddress address = new DeliveryAddress(
                null,
                dto.getUserUid() != null ? Long.valueOf(dto.getUserUid()) : null,
                dto.getSocialUid() != null ? Long.valueOf(dto.getSocialUid()) : null,
                dto.getMerchantUid(),
                dto.getDeliveryAddress().getAddressStart(),
                dto.getDeliveryAddress().getAddressStartLat(),
                dto.getDeliveryAddress().getAddressStartLan(),
                dto.getDeliveryAddress().getAddressDestination(),
                dto.getDeliveryAddress().getAddressDestinationLat(),
                dto.getDeliveryAddress().getAddressDestinationLan()
        );

        return txOp.transactional(
                orderRepository.saveAll(ordersToSave).then(deliveryAddressRepository.save(address))
        ).then(Mono.defer(() -> {
            OrderCreatedMessage msg = OrderCreatedMessage.builder()
                    .merchantUid(dto.getMerchantUid())
                    .status(dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED)
                    //.createdDate(getNow())
                    //.reservationDate(dto.getReservationDate())
                    .build();

            streamBridge.send("orderCreated-out-0", MessageBuilder.withPayload(msg).build());

            return Mono.just(OrderResponseDTO.builder()
                    .success(true)
                    .message("주문 저장 + 상태 MQ 발행 완료")
                    .orderUid(ordersToSave.get(0).getUid()) // 대표 uid 사용
                    .orderUids(ordersToSave.stream().map(Order::getUid).toList())
                    .build());
        }));
    }

    // 업데이트 요청을 queue에서 받음
    public Mono<Void> updateOrderFromMessage(OrderCreatedMessage message) {
        log.info("[updateOrderFromMessage] 상태 업데이트 시작: merchantUid={}, newStatus={}", message.merchantUid(), message.status());

        return orderRepository.findByMerchantUid(message.merchantUid())
                .collectList()
                .flatMap(existingOrders -> {
                    if (existingOrders.isEmpty()) {
                        log.warn("[updateOrderFromMessage] 주문 없음 → 상태 업데이트 생략 (submitOrder에서 저장해야 함)");
                        return Mono.empty();
                    }

                    boolean statusChanged = existingOrders.stream()
                            .anyMatch(o -> o.getStatus() != message.status());

                    if (!statusChanged) {
                        log.info("[updateOrderFromMessage] 상태 동일 → 생략");
                        return Mono.empty();
                    }

                    List<Order> updatedOrders = existingOrders.stream()
                            .filter(orig -> orig.getVersion() == 0)
                            .map(orig -> orig.builder()
                                    .uid(orig.getUid())
                                    .userUid(orig.getUserUid())
                                    .storeUid(orig.getStoreUid())
                                    .merchantUid(orig.getMerchantUid())
                                    .menuName(orig.getMenuName())
                                    .amount(orig.getAmount())
                                    .price(orig.getPrice())
                                    .calorie(orig.getCalorie())
                                    .payment(orig.getPayment())
                                    .status(message.status())
                                    .createdDate(orig.getCreatedDate())
                                    .reservationDate(orig.getReservationDate())
                                    .version(orig.getVersion())
                                    .build()
                            ).toList();

                    return txOp.transactional(
                            orderRepository.saveAll(updatedOrders)
                                    .doOnNext(order -> log.info("[updateOrderFromMessage] 저장된 주문: uid={}, status={}", order.getUid(), order.getStatus()))
                                    .then()
                    );

                })
                .onErrorResume(e -> {
                    log.error("[updateOrderFromMessage] 주문 상태 변경 실패: {}", e.getMessage(), e);
                    return Mono.error(e); // 에러를 다시 던져서 상위 로직까지 전파
                });
    }

    // 상태 업데이트 (DB에서)
    public Mono<OrderStatusChangeResponseDTO> changeOrderStatus(String merchantUid, OrderStatus newStatus) {
        validateStatusForQueue(newStatus); // MQ 발행 가능한 상태인지 체크

        return orderRepository.findByMerchantUid(merchantUid)
                .collectList()
                .flatMap(existingOrders -> {
                    if (existingOrders.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("해당 merchantUid로 주문을 찾을 수 없습니다."));
                    }

                    List<Order> updatedOrders = existingOrders.stream()
                            .map(orig -> Order.builder()
                                    .uid(orig.getUid())
                                    .userUid(orig.getUserUid())
                                    .storeUid(orig.getStoreUid())
                                    .merchantUid(orig.getMerchantUid())
                                    .menuName(orig.getMenuName())
                                    .amount(orig.getAmount())
                                    .price(orig.getPrice())
                                    .calorie(orig.getCalorie())
                                    .payment(orig.getPayment())
                                    .status(newStatus)
                                    .createdDate(orig.getCreatedDate())
                                    .reservationDate(orig.getReservationDate())
                                    .version(orig.getVersion())
                                    .build()
                            )
                            .toList();

                    return txOp.transactional(orderRepository.saveAll(updatedOrders).then()).thenReturn(updatedOrders);
                })
                .flatMap(savedOrders -> {
                    // MQ 메시지 발행
                    OrderCreatedMessage msg = OrderCreatedMessage.builder()
                            .merchantUid(merchantUid)
                            .status(newStatus)
                            //.createdDate(getNow())
                            .build();

                    streamBridge.send("orderCreated-out-0", MessageBuilder.withPayload(msg).build());

                    return Mono.just(OrderStatusChangeResponseDTO.builder()
                            .success(true)
                            .message("상태 변경 및 MQ 발행 완료")
                            .merchantUid(merchantUid)
                            .newStatus(newStatus)
                            .build());
                })
                .onErrorResume(e -> {
                    log.error("[changeOrderStatus] 오류 발생: {}", e.getMessage(), e);
                    return Mono.just(OrderStatusChangeResponseDTO.builder()
                            .success(false)
                            .message("상태 변경 실패: " + e.getMessage())
                            .merchantUid(merchantUid)
                            .newStatus(newStatus)
                            .build());
                });
    }
    // 결제 사전 검증 + order_created 상태 주문 저장
    public Mono<PreparePaymentResponseDTO> preparePayment(PreparePaymentRequestDTO req) {
        // 1) 예약시간 처리
        LocalDateTime reservationTime = req.getReservationDate();

        // 2) Order 엔티티 생성 (reservationDate 에 위에서 가공한 값을 넣는다)
        Order toSave = Order.builder()
                .merchantUid(req.getMerchantUid())
                .storeUid(req.getStoreUid())
                .menuName(req.getMenuName())
                .amount(1)
                .payment("card")
                .status(OrderStatus.ORDER_CREATED)
                .price(req.getTotalPrice())
                .calorie(0.0)
                .userUid(req.getUserUid())
                .reservationDate(reservationTime)
                .build();

        // 3) 트랜잭션 안에서 저장하고 DTO 로 변환
        return txOp.transactional(
                orderRepository.findByMerchantUid(req.getMerchantUid())
                        .filter(order -> order.getStatus() == OrderStatus.ORDER_CREATED)
                        .flatMap(orderRepository::delete)
                        .then(orderRepository.save(toSave))
        ).map(saved -> PreparePaymentResponseDTO.builder()
                .merchantUid(saved.getMerchantUid())
                .requestedAmount(saved.getPrice())
                .version(saved.getVersion())
                .message("사전 검증 및 저장 완료")
                .build());
    }

    // 결제 성공 처리 → 주문 상태 PAYMENT_COMPLETED로 변경
    public Mono<Void> updateOrderStatusToSuccess(String merchantUid) {
        return txOp.execute(tx ->
                orderRepository.findByMerchantUid(merchantUid)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid)))
                        .flatMap(orig -> {

                            // 기존 데이터에서 버전을 유지하며 status 만 변경
                            Order updated = Order.builder()
                                    .uid(orig.getUid())
                                    .userUid(orig.getUserUid())
                                    .storeUid(orig.getStoreUid())
                                    .merchantUid(orig.getMerchantUid())
                                    .menuName(orig.getMenuName())
                                    .amount(orig.getAmount())
                                    .price(orig.getPrice())
                                    .calorie(orig.getCalorie())
                                    .payment(orig.getPayment())
                                    .status(OrderStatus.PAYMENT_COMPLETED)
                                    .createdDate(orig.getCreatedDate())
                                    .reservationDate(orig.getReservationDate())
                                    .version(orig.getVersion())
                                    .build();
                            return orderRepository.save(updated);
                        })
        ).then(orderRepository.deleteRepresentativeOrder(merchantUid));
    }



    // 결제 실패 처리 → 주문 상태 PAYMENT_FAILED 변경 + 대표 주문 삭제
    public Mono<Void> updateOrderStatusToFailed(String merchantUid) {
        return txOp.execute(tx ->
                orderRepository.findByMerchantUid(merchantUid)
                        .filter(order -> order.getVersion() == 0)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid)))
                        .flatMap(orig -> {

                            Order updated = Order.builder()
                                    .uid(orig.getUid())
                                    .userUid(orig.getUserUid())
                                    .storeUid(orig.getStoreUid())
                                    .merchantUid(orig.getMerchantUid())
                                    .menuName(orig.getMenuName())
                                    .amount(orig.getAmount())
                                    .price(orig.getPrice())
                                    .calorie(orig.getCalorie())
                                    .payment(orig.getPayment())
                                    .status(OrderStatus.PAYMENT_FAILED)
                                    .createdDate(orig.getCreatedDate())
                                    .reservationDate(orig.getReservationDate())
                                    .version(orig.getVersion())
                                    .build();
                            return orderRepository.save(updated);
                        })
                        .then(orderRepository.deleteRepresentativeOrder(merchantUid))
        ).then();
    }

}
