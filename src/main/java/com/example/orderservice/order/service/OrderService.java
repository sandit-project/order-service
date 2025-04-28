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
        return Flux.fromIterable(dto.getItems())
                .flatMap(item -> cartRepository.findById(item.uid())
                        .doOnNext(c -> log.info("카트 조회 성공: {}", c))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않은 카트: " + item.uid())))
                        .flatMap(cart -> {
                            LocalDateTime resv = dto.getReservationDate();
                            if (resv != null && Duration.between(resv.truncatedTo(ChronoUnit.MINUTES),
                                    getNow().truncatedTo(ChronoUnit.MINUTES)).abs().toMinutes() < 5) {
                                resv = null;
                            }
                            Order order = Order.builder()
                                    .userUid(dto.getUserUid())
                                    .socialUid(dto.getSocialUid())
                                    .storeUid(dto.getStoreUid())
                                    .merchantUid(dto.getMerchantUid())
                                    .menuName(cart.menuName())
                                    .amount(item.amount())
                                    .price(item.unitPrice())
                                    .calorie(cart.calorie())
                                    .payment(dto.getPayment())
                                    .status(dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED)
                                    .reservationDate(resv)
                                    .build();
                            return orderRepository.save(order)
                                    .doOnNext(o -> log.info("주문 저장 성공: {}", o));
                        })
                )
                .collectList()
                .flatMap(orders -> {
                    log.info("진입함: 총 저장된 주문 개수 = {}", orders.size());
                    if (orders.isEmpty()) {
                        return Mono.error(new RuntimeException("주문 저장 실패"));
                    }
                    Order first = orders.get(0);
                    DeliveryAddressDTO addrDto = dto.getDeliveryAddress();
                    DeliveryAddress address = DeliveryAddress.builder()
                            .userUid(dto.getUserUid() != null ? dto.getUserUid().longValue() : null)
                            .socialUid(dto.getSocialUid() != null ? dto.getSocialUid().longValue() : null)
                            .merchantUid(first.merchantUid())
                            .addressStart(addrDto.getAddressStart())
                            .addressStartLat(addrDto.getAddressStartLat())
                            .addressStartLan(addrDto.getAddressStartLan())
                            .addressDestination(addrDto.getAddressDestination())
                            .addressDestinationLat(addrDto.getAddressDestinationLat())
                            .addressDestinationLan(addrDto.getAddressDestinationLan())
                            .build();

                    return deliveryAddressRepository.save(address)
                            .doOnSuccess(a -> log.info("배송주소 저장 성공: {}", a))
                            .doOnError(e -> log.error("배송주소 저장 실패: ", e))
                            .thenReturn(OrderResponseDTO.builder()
                                    .success(true)
                                    .message("주문 및 배송주소 저장 완료")
                                    .orderUid(first.uid())
                                    .build());
                })
                .as(txOp::transactional)
                .onErrorResume(e -> Mono.just(
                        OrderResponseDTO.builder()
                                .success(false)
                                .message("서버 에러: " + e.getMessage())
                                .build()
                ));
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
