package com.example.orderservice.order.service;

import com.example.orderservice.cart.CartRepository;
import com.example.orderservice.cart.CartService;
import com.example.orderservice.order.domain.*;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final TransactionalOperator txOp;
    private final DeliveryAddressRepository deliveryAddressRepository;

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

    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
        // 1) Flux 정의 (기존 로직 그대로)
        Flux<Order> creates = Flux.fromIterable(dto.getItems())
                .flatMap(item -> cartRepository.findById(item.cartUid())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않은 카트: " + item.cartUid())))
                        .map(cart -> {
                            // 예약시간 5분 기준 처리
                            LocalDateTime resv = dto.getReservationDate();
                            if (resv != null && Duration.between(resv.truncatedTo(ChronoUnit.MINUTES),
                                            getNow().truncatedTo(ChronoUnit.MINUTES))
                                    .abs().toMinutes() < 5) {
                                resv = null;
                            }
                            return Order.builder()
                                    .userUid(dto.getUserUid())
                                    .storeUid(dto.getStoreUid())
                                    .merchantUid(dto.getMerchantUid())
                                    .menuName(cart.menuName())
                                    .amount(cart.amount())
                                    .price(cart.price())
                                    .calorie(cart.calorie())
                                    .payment(dto.getPayment())
                                    .status(dto.isPaymentSuccess()
                                            ? OrderStatus.PAYMENT_COMPLETED
                                            : OrderStatus.PAYMENT_FAILED)
                                    .reservationDate(resv)
                                    .build();
                        })
                        .flatMap(orderRepository::save)  // save() 시 @Version 체크
                );

        // 2) 트랜잭션으로 감싸기 → 실패 시 롤백
        return txOp.execute(tx -> creates.collectList())
                .flatMap(orders -> {
                            if (orders.isEmpty()) {
                                return Mono.just(
                                        OrderResponseDTO.builder()
                                                .success(false)
                                                .message("주문 저장 실패")
                                                .build()
                                );
                            }

                            Integer orderUid = orders.get(0).uid();

                    DeliveryAddress addr = new DeliveryAddress(
                            null,
                            dto.getUserUid(),
                            dto.getSocialUid(),
                            dto.getMerchantUid(),
                            dto.getAddressStart(),
                            dto.getAddressStartLat(),
                            dto.getAddressStartLan(),
                            dto.getAddressDestination(),
                            dto.getAddressDestinationLat(),
                            dto.getAddressDestinationLan()
                    );

                    return deliveryAddressRepository.save(addr)
                            .thenReturn(
                                    OrderResponseDTO.builder()
                                            .success(true)
                                            .message("주문 및 배송주소 저장 완료")
                                            .orderUid(orderUid)
                                            .build()
                            );
                })
                .single()
                .onErrorResume(e ->
                        Mono.just(
                                OrderResponseDTO.builder()
                                        .success(false)
                                        .message("서버 에러: " + e.getMessage())
                                        .build()
                        )
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
                        .merchantUid(saved.merchantUid())
                        .requestedAmount(saved.price())
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
                                    .uid(orig.uid())
                                    .userUid(orig.userUid())
                                    .storeUid(orig.storeUid())
                                    .merchantUid(orig.merchantUid())
                                    .menuName(orig.menuName())
                                    .amount(orig.amount())
                                    .price(orig.price())
                                    .calorie(orig.calorie())
                                    .payment(orig.payment())
                                    .status(OrderStatus.PAYMENT_COMPLETED)
                                    .createdDate(orig.createdDate())
                                    .reservationDate(orig.reservationDate())
                                    .version(orig.version())
                                    .build();
                            return orderRepository.save(updated);
                        })
        ).then();
    }

    //결제 실패 후 상태 업데이트
    public Mono<Void> updateOrderStatusToFailed(String merchantUid) {
        return txOp.execute(tx ->
                orderRepository.findByMerchantUid(merchantUid)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid)))
                        .flatMap(orig -> {
                            Order updated = Order.builder()
                                    .uid(orig.uid())
                                    .userUid(orig.userUid())
                                    .storeUid(orig.storeUid())
                                    .merchantUid(orig.merchantUid())
                                    .menuName(orig.menuName())
                                    .amount(orig.amount())
                                    .price(orig.price())
                                    .calorie(orig.calorie())
                                    .payment(orig.payment())
                                    .status(OrderStatus.PAYMENT_FAILED)
                                    .createdDate(orig.createdDate())
                                    .reservationDate(orig.reservationDate())
                                    .version(orig.version())
                                    .build();
                            return orderRepository.save(updated);
                        })
        ).then();
    }

}
