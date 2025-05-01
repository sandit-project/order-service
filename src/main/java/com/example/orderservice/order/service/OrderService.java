package com.example.orderservice.order.service;

import com.example.orderservice.event.DeliveryAddressMessage;
import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.event.OrderItemMessage;
import com.example.orderservice.menu.CartResponseDTO;
import com.example.orderservice.menu.MenuClient;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final TransactionalOperator txOp;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StreamBridge streamBridge;
    private final MenuClient menuClient;

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

    //RabbitMQ 메시지 받아서 주문 저장
    public Mono<Void> saveOrderFromMessage(OrderCreatedMessage message) {
        log.info("[saveOrderFromMessage] 수신한 메시지: merchantUid={}, status={}, itemCount={}",
                message.merchantUid(), message.status(), message.items().size());

        message.items().forEach(item ->
                log.info("[saveOrderFromMessage] item: name={}, version={}", item.menuName(), item.version())
        );

        return orderRepository.findByMerchantUid(message.merchantUid())
                .filter(order -> order.getStatus() == OrderStatus.ORDER_CREATED)
                .flatMap(orderRepository::delete)
                .then(Mono.defer(() -> {
                    List<Order> ordersToSave = message.items().stream()
                            .filter(item -> item.version() == 0)
                            .map(item -> Order.builder()
                                    .userUid(message.userUid())
                                    .socialUid(message.socialUid())
                                    .storeUid(message.storeUid())
                                    .merchantUid(message.merchantUid())
                                    .menuName(item.menuName())
                                    .amount(item.amount())
                                    .price(item.unitPrice())
                                    .payment("card")
                                    .status(message.status())
                                    .createdDate(message.createdDate())
                                    .calorie(item.calorie())
                                    .reservationDate(message.reservationDate())
                                    .build()
                            )
                            .toList();

                    DeliveryAddress address = new DeliveryAddress(
                            null,
                            message.userUid() != null ? Long.valueOf(message.userUid()) : null,
                            message.socialUid() != null ? Long.valueOf(message.socialUid()) : null,
                            message.merchantUid(),
                            message.deliveryAddress().addressStart(),
                            message.deliveryAddress().addressStartLat(),
                            message.deliveryAddress().addressStartLan(),
                            message.deliveryAddress().addressDestination(),
                            message.deliveryAddress().addressDestinationLat(),
                            message.deliveryAddress().addressDestinationLan()
                    );

                    return txOp.transactional(
                            orderRepository.saveAll(ordersToSave).then(deliveryAddressRepository.save(address))
                    ).then();
                }));
    }

    // 상태 변경
    public Mono<Void> updateOrderFromMessage(OrderCreatedMessage message) {
        log.info("[updateOrderFromMessage] merchantUid={}, newStatus={}", message.merchantUid(), message.status());

        return orderRepository.findByMerchantUid(message.merchantUid())
                .collectList()
                .flatMap(existingOrders -> {
                    if (existingOrders.isEmpty()) {
                        log.warn("[updateOrderFromMessage] 기존 주문 없음, 새로 저장함.");
                        return saveOrderFromMessage(message); // 신규 저장
                    }

                    // 상태 변화가 없으면 무시
                    boolean statusChanged = existingOrders.stream()
                            .anyMatch(o -> o.getStatus() != message.status());

                    if (!statusChanged) {
                        log.info("[updateOrderFromMessage] 모든 주문이 이미 동일 상태임 → 업데이트 생략");
                        return Mono.empty();
                    }

                    // 상태 업데이트용 객체 생성 (uid 등 필수값 포함!)
                    List<Order> updatedOrders = existingOrders.stream()
                            .map(orig -> Order.builder()
                                    .uid(orig.getUid()) // 꼭 있어야 UPDATE 가능
                                    .userUid(orig.getUserUid())
                                    .storeUid(orig.getStoreUid())
                                    .merchantUid(orig.getMerchantUid())
                                    .menuName(orig.getMenuName())
                                    .amount(orig.getAmount())
                                    .price(orig.getPrice())
                                    .payment(orig.getPayment())
                                    .calorie(orig.getCalorie())
                                    .status(message.status()) // 상태만 바꿈
                                    .createdDate(orig.getCreatedDate())
                                    .reservationDate(orig.getReservationDate())
                                    .version(orig.getVersion()) // 낙관적 락 보호
                                    .build()
                            )
                            .toList();

                    return txOp.transactional(orderRepository.saveAll(updatedOrders).then());
                })
                .onErrorResume(e -> {
                    log.error("[updateOrderFromMessage] 주문 상태 업데이트 실패: {}", e.getMessage(), e);
                    return Mono.empty(); // fallback 로직 필요하면 여기에
                });
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

    // 주문 저장 요청 처리 → MQ 발행
    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
        log.info("[submitOrder] 요청 들어옴: merchantUid={}, version={}", dto.getMerchantUid(), dto.getVersion());

        if (dto.getVersion() != 0) {
            log.warn("[submitOrder] 대표주문이므로 처리하지 않음: merchantUid={}", dto.getMerchantUid());
            return Mono.empty();
        }

        return orderRepository.deletePreOrders(dto.getMerchantUid(), OrderStatus.ORDER_CREATED)
                .then(Mono.defer(() -> {
                    OrderCreatedMessage message = createOrderCreatedMessage(dto);
                    validateStatusForQueue(message.status());

                    log.info("[submitOrder] MQ 발행 준비 완료: merchantUid={}, 상태={}", message.merchantUid(), message.status());

                    streamBridge.send("orderCreated-out-0", MessageBuilder.withPayload(message).build());

                    return Mono.just(OrderResponseDTO.builder()
                            .success(true)
                            .message("주문 요청이 MQ로 발행되었습니다.")
                            .build());
                }));
    }

    // OrderRequestDTO를 OrderCreatedMessage로 변환
    private OrderCreatedMessage createOrderCreatedMessage(OrderRequestDTO dto) {
        List<OrderItemMessage> items = dto.getItems().stream()
                .filter(item -> item.version() == 0)
                .map(item -> new OrderItemMessage(
                        item.menuName(),
                        item.amount(),
                        item.calorie(),
                        item.unitPrice(),
                        item.version()
                ))
                .toList();

        //주소가 누락되면 이벤트 메시지 미발행
        if (dto.getDeliveryAddress() == null) {
            throw new IllegalArgumentException("배송 주소가 누락되었습니다.");
        }

        return new OrderCreatedMessage(
                dto.getMerchantUid(),
                dto.getUserUid(),
                dto.getSocialUid(),
                null,
                null,
                dto.getStoreUid(),
                new DeliveryAddressMessage(
                        dto.getDeliveryAddress().getAddressStart(),
                        dto.getDeliveryAddress().getAddressStartLat(),
                        dto.getDeliveryAddress().getAddressStartLan(),
                        dto.getDeliveryAddress().getAddressDestination(),
                        dto.getDeliveryAddress().getAddressDestinationLat(),
                        dto.getDeliveryAddress().getAddressDestinationLan()
                ),
                items,
                dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED,
                getNow(),
                dto.getReservationDate()
        );

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
        ).then();
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
