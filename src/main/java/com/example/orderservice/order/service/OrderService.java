package com.example.orderservice.order.service;

import com.example.orderservice.event.DeliveryAddressMessage;
import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.event.OrderItemMessage;
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

    // 현재 시각을 반환하는 헬퍼 메서드 (테스트 시 오버라이드 용)
    protected LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public Flux<Order> findAllOrders() {
        return orderRepository.findAllOrders();
    }

    public Mono<Order> getOrderByUid(Integer uid) {
        return orderRepository.findById(uid);
    }

    public Flux<Order> findAllByUserUid(Integer userUid) {
        return orderRepository.findByUserUid(userUid);
    }

    //RabbitMQ 메시지 받아서 주문 저장
    public Mono<Void> saveOrderFromMessage(OrderCreatedMessage message) {
        // 1. 사전검증용 ORDER_CREATED 주문 삭제
        Mono<Void> deleteOld = orderRepository.findByMerchantUid(message.merchantUid())
                .filter(order -> order.getStatus() == OrderStatus.ORDER_CREATED)
                .flatMap(orderRepository::delete)
                .then();

        List<Order> ordersToSave = message.items().stream()
                .filter(item -> item.version() == 0) // 버전 0인 것만 저장
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
                        .reservationDate(null)
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

        return deleteOld.then(
                txOp.transactional(
                        orderRepository.saveAll(ordersToSave).then(deliveryAddressRepository.save(address))
                ).then()
        );
    }


    //프론트 요청으로 주문 저장
//    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
//        if (dto.getVersion() != 0) {
//            // 대표 주문이면 바로 저장 중단
//            return Mono.empty();
//        }
//
//        return txOp.transactional(
//                Flux.fromIterable(dto.getItems())
//                        .filter(item -> item.version() == 0)
//                        .map(item -> Order.builder()
//                                .userUid(dto.getUserUid())
//                                .socialUid(dto.getSocialUid())
//                                .storeUid(dto.getStoreUid())
//                                .merchantUid(dto.getMerchantUid())
//                                .menuName(item.menuName())     // DTO의 필드를 직접 사용
//                                .amount(item.amount())
//                                .price(item.unitPrice())
//                                .calorie(item.calorie())
//                                .payment(dto.getPayment())
//                                .status(OrderStatus.valueOf(String.valueOf(dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED)))
//                                .reservationDate(dto.getReservationDate())
//                                .build()
//                        )
//                        .collectList()
//                        .flatMap(orderList ->
//                                orderRepository.saveAll(orderList)
//                                        .collectList()
//                                        .flatMap(savedOrders -> {
//                                            if (savedOrders.isEmpty()) {
//                                                return Mono.error(new RuntimeException("주문 저장 실패"));
//                                            }
//                                            Integer orderUid = savedOrders.get(0).getUid();
//
//                                            DeliveryAddressDTO addressDTO = dto.getDeliveryAddress();
//                                            DeliveryAddress address = new DeliveryAddress(
//                                                    null,
//                                                    dto.getUserUid() != null ? Long.valueOf(dto.getUserUid()) : null,
//                                                    dto.getSocialUid() != null ? Long.valueOf(dto.getSocialUid()) : null,
//                                                    savedOrders.get(0).getMerchantUid(),
//                                                    addressDTO.getAddressStart(),
//                                                    addressDTO.getAddressStartLat(),
//                                                    addressDTO.getAddressStartLan(),
//                                                    addressDTO.getAddressDestination(),
//                                                    addressDTO.getAddressDestinationLat(),
//                                                    addressDTO.getAddressDestinationLan()
//                                            );
//
//                                            log.info("save address : {}", address);
//                                            return deliveryAddressRepository.save(address)
//
//                                                    .doOnSuccess(savedAddress -> {
//                                                        publishOrderCreatedEvent(savedOrders, savedAddress);
//                                                    })
//
//                                                    .thenReturn(
//                                                            OrderResponseDTO.builder()
//                                                                    .success(true)
//                                                                    .message("주문 및 배송주소 저장 완료")
//                                                                    .orderUid(orderUid)
//                                                                    .build()
//                                                    );
//                                        })
//                        )
//        ).single();
//    }

    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
        if (dto.getVersion() != 0) {
            return Mono.empty(); // 대표주문 필터
        }

        return orderRepository.deleteRepresentativeOrder(dto.getMerchantUid())
                .then(Mono.defer(() -> {
                    OrderCreatedMessage message = createOrderCreatedMessage(dto);
                    streamBridge.send("orderCreated-out-0", MessageBuilder.withPayload(message).build());

                    return Mono.just(OrderResponseDTO.builder()
                            .success(true)
                            .message("주문 요청이 MQ로 발행되었습니다.")
                            .build());
                }));
    }


    // createOrderCreatedMessage
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

        return new OrderCreatedMessage(
                dto.getMerchantUid(),
                dto.getUserUid(),
                dto.getSocialUid(),
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
                null
        );
    }





    // 결제 사전 검증
    public Mono<PreparePaymentResponseDTO> preparePayment(PreparePaymentRequestDTO req) {
        // 1) 예약시간 처리
        LocalDateTime reservationTime = req.getReservationDate();
        if (reservationTime != null) {
            LocalDateTime nowTrunc   = getNow().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime inputTrunc = reservationTime.truncatedTo(ChronoUnit.MINUTES);
            if (Duration.between(inputTrunc, nowTrunc).abs().toMinutes() < 5) {
                reservationTime = null;
            }
        }

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
        return txOp.execute(tx -> orderRepository.save(toSave))
                .single()
                .map(saved -> PreparePaymentResponseDTO.builder()
                        .merchantUid(saved.getMerchantUid())
                        .requestedAmount(saved.getPrice())
                        .message("사전 검증 및 저장 완료")
                        .build());
    }

    //결제 성공 후 상태 업데이트
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

    //결제 실패 후 상태 업데이트
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
