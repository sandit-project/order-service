package com.example.orderservice.order.service;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.DeliveryAddress;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.payment.CancelPaymentResponseDTO;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderCancelRedisRepository redisRepo;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final TransactionalOperator txOp;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StreamBridge streamBridge;
    private final RedissonClient redissonClient;

    // 현재 시각을 반환하는 헬퍼 메서드 (테스트 시 오버라이드 용)
    protected LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    // 모든 주문 조회
    public Flux<Order> findAllOrders() {
        return orderRepository.findAllOrders();
    }

    // 조리중 상태 주문 조회
    public Flux<DeliveryOrderResponseDTO> getCookingOrders() {
        return deliveryOrderRepository.getCookingOrders();
    }

    // 배달중 상태 주문 조회
    public Flux<DeliveryOrderResponseDTO> getDeliveringOrders() {
        return deliveryOrderRepository.getDeliveringOrders();
    }

    // uid로 주문 조회 (단건)
    public Mono<Order> getOrderByUid(Integer uid) {
        return orderRepository.findById(uid);
    }

    //merchant_uid로 주문 조회
    public Flux<Order> getOrderByMerchantUid(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid);
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

    // 결제 전 사전 검증 및 주문 생성
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

    // 실제 주문 저장 + 배송주소 저장 + MQ 발행을 락으로 감쌈
    public Mono<OrderResponseDTO> submitOrder(OrderRequestDTO dto) {
        String merchantUid = dto.getMerchantUid();
        String lockKey = "order:lock:" + merchantUid;
        RLock lock = redissonClient.getLock(lockKey);

        return Mono.fromCallable(() -> {
                    lock.lock(); // 순서대로 처리됨. 대기 → 실행 → 해제 → 다음 실행
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic()) // 락은 블로킹 → 별도 스레드에서 실행
                .flatMap(ignore -> runSubmitLogic(dto))
                .doFinally(signal -> {
                    log.info("락 상태: held={}, key={}", lock.isHeldByCurrentThread(), lockKey);
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.info("락 해제 완료: {}", lockKey);
                    }
                });
    }


    // 실제 주문 저장 + 배송주소 저장 + MQ 발행
    public Mono<OrderResponseDTO> runSubmitLogic(OrderRequestDTO dto) {

        log.info("[submitOrder] 요청: merchantUid={}, version={}", dto.getMerchantUid(), dto.getVersion());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return Mono.error(new IllegalArgumentException("주문 항목이 없습니다."));
        }
        if (dto.getVersion() != 0) {
            log.error("대표주문 skipping: merchantUid={}", dto.getMerchantUid());
            return Mono.empty();
        }

        List<Order> orders = dto.getItems().stream()
                .filter(item -> {
                    int ver = item.version() != null ? item.version() : 0;
                    return ver == 0
                    && !(item.menuName().contains("외") && item.amount() == 1);
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
                        .build())
                .toList();

        return txOp.transactional(
                        orderRepository.saveAll(orders).collectList()
                                .flatMap(savedOrders -> {
                                    var addr = dto.getDeliveryAddress();
                                    if (addr == null) {
                                        return Mono.just(savedOrders);
                                    }
                                    if (addr.getAddressStart() == null || addr.getAddressDestination() == null) {
                                        return Mono.error(new IllegalArgumentException("배송주소 정보가 부족합니다."));
                                    }

                                    return deliveryAddressRepository.existsByMerchantUid(dto.getMerchantUid())
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    log.info("배송 주소 이미 존재: {}", dto.getMerchantUid());
                                                    return Mono.just(savedOrders); // skip
                                                }

                                                DeliveryAddress addressEntity = new DeliveryAddress();
                                                addressEntity.setMerchantUid(dto.getMerchantUid());
                                                addressEntity.setUserUid(Long.valueOf(dto.getUserUid()));
                                                addressEntity.setAddressStart(addr.getAddressStart());
                                                addressEntity.setAddressStartLat(addr.getAddressStartLat());
                                                addressEntity.setAddressStartLan(addr.getAddressStartLan());
                                                addressEntity.setAddressDestination(addr.getAddressDestination());
                                                addressEntity.setAddressDestinationLat(addr.getAddressDestinationLat());
                                                addressEntity.setAddressDestinationLan(addr.getAddressDestinationLan());

                                                return deliveryAddressRepository.save(addressEntity)
                                                        .thenReturn(savedOrders);
                                            });
                                })
                ).flatMap(savedOrders -> {
            // 이벤트 생성 및 발행 (트랜잭션 바깥에서)
            OrderCreatedMessage event = OrderCreatedMessage.builder()
                    .merchantUid(dto.getMerchantUid())
                    .status(dto.isPaymentSuccess() ? OrderStatus.PAYMENT_COMPLETED : OrderStatus.PAYMENT_FAILED)
                    .deliveryAcceptTime(null)
                    .deliveredTime(null)
                    .riderUserUid(null)
                    .riderSocialUid(null)
                    .addressStart(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getAddressStart() : null)
                    .addressDestination(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getAddressDestination() : null)
                    .build();

            streamBridge.send("orderCreated-out-0", MessageBuilder.withPayload(event).build());

            return Mono.just(OrderResponseDTO.builder()
                    .success(true)
                    .message("주문 + 배송주소 저장 및 이벤트 발행 완료")
                    .orderUid(savedOrders.get(0).getUid())
                    .orderUids(savedOrders.stream().map(Order::getUid).toList())
                    .build());
        });
    }


    // 주문 상태 변경 및 MQ 발행
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

    // 결제 성공 처리 → 주문 상태 PAYMENT_COMPLETED로 변경
    public Mono<Void> updateOrderStatusToSuccess(String merchantUid) {
        return txOp.execute(tx ->
                orderRepository.findByMerchantUid(merchantUid)
                        .filter(o -> o.getVersion() == 0)
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

    // 1) init: 주문들 조회 → Redis에 oldStatus 저장 → DB 상태 CANCELLED */
    public Mono<CancelPaymentResponseDTO> initCancel(String merchantUid) {
        return orderRepository.findByMerchantUid(merchantUid)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        log.error("[initCancel] 주문 없음: merchantUid={}", merchantUid);
                        return Mono.error(new IllegalArgumentException("주문을 찾을 수 없습니다."));
                    }
                    String old = list.get(0).getStatus().name();
                    return redisRepo.savePreviousState(merchantUid, old)
                            .thenMany(Flux.fromIterable(list))
                            .flatMap(o -> {
                                o.setStatus(OrderStatus.ORDER_CANCELLED);
                                return orderRepository.save(o);
                            })
                            .then(Mono.just(new CancelPaymentResponseDTO(true, "상태 변경 성공")));
                });
    }

    // 2) confirm: 취소 최종 확정 → Redis 키 삭제 */
    public Mono<CancelPaymentResponseDTO> confirmCancel(String merchantUid) {
        return redisRepo.deleteState(merchantUid).then(Mono.just(new CancelPaymentResponseDTO(true, "Redis 키 삭제 성공")));
    }

    // 3) compensate: 실패 시 보상 → Redis 에서 oldStatus 꺼내와서 롤백 → 키 삭제
    public Mono<CancelPaymentResponseDTO> compensateCancel(String merchantUid) {
        return redisRepo.fetchPreviousState(merchantUid)
                .flatMapMany(prevName -> {
                    OrderStatus prev = OrderStatus.valueOf(prevName);
                    return orderRepository.findByMerchantUid(merchantUid)
                            .flatMap(o -> {
                                o.setStatus(prev);
                                return orderRepository.save(o);
                            });
                })
                .then(redisRepo.deleteState(merchantUid))
                .then(Mono.just(new CancelPaymentResponseDTO(true, "롤백 및 Redis 키 삭제 완료")));
    }

    // 상태 변경 실패 시 DB 롤백 후 보상 메시지 발행
    public Mono<Void> updateStatusWithRollback(String merchantUid, OrderStatus from, OrderStatus to) {
        return orderRepository.findByMerchantUid(merchantUid)
                .collectList()
                .flatMap(existingOrders -> {
                    if (existingOrders.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("주문 없음"));
                    }

                    // 현재 상태가 from 인지 확인
                    if (existingOrders.stream().anyMatch(o -> o.getStatus() != from)) {
                        return Mono.error(new IllegalStateException("상태 전이 불가"));
                    }

                    List<Order> updated = existingOrders.stream()
                            .map(o -> {
                                o.setStatus(to);
                                return o;
                            }).toList();

                    return orderRepository.saveAll(updated).then();
                })
                .onErrorResume(e -> {
                    log.warn("상태 변경 실패, 롤백 수행 중...: {}", e.getMessage());

                    // 롤백: from 상태로 되돌림
                    return orderRepository.findByMerchantUid(merchantUid)
                            .flatMap(order -> {
                                order.setStatus(from); // 롤백
                                return orderRepository.save(order);
                            })
                            .then(Mono.fromRunnable(() -> {
                                OrderCreatedMessage rollbackMsg = OrderCreatedMessage.builder()
                                        .merchantUid(merchantUid)
                                        .status(from)
                                        .build();
                                streamBridge.send("orderRollback-out-0", MessageBuilder.withPayload(rollbackMsg).build());
                            }));
                }).then();
    }



    /**
     * 지점별·상태별 주문 조회
     */
    public Flux<DeliveryOrderResponseDTO> getStoreOrders(Integer storeUid, String status) {
        // status가 null이면 기본값 PAYMENT_COMPLETED 사용
        String targetStatus = (status != null ? status : OrderStatus.PAYMENT_COMPLETED.name());
        return deliveryOrderRepository.getStoreOrdersByStatusAndStoreUid(storeUid, targetStatus);
    }

}
