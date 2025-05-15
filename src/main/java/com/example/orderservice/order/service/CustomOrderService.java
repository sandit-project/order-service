package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.*;
import com.example.orderservice.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;
    private final TransactionalOperator txOp;
    private final RedissonClient redissonClient;

    public Flux<CustomOrder> findAllOrders() {
        return customOrderRepository.findAll();
    }

    public Mono<CustomOrder> findByUid(Integer uid) {
        return customOrderRepository.findById(uid);
    }

    public Mono<OrderResponseDTO> submitFinalOrder(FinalCustomOrderRequest finalRequestDTO) {
        List<CustomOrderRequestDTO> customList = Optional.ofNullable(finalRequestDTO.getCustomOrderRequestDTO())
                .orElse(Collections.emptyList());

        if (customList.isEmpty()) {
            return Mono.empty();
        }

        String merchantUid = finalRequestDTO.getOrderRequestDTO().getMerchantUid();
        String lockKey = "order:lock:" + merchantUid;
        RLock lock = redissonClient.getLock(lockKey);

        return Mono.fromCallable(() -> {
                    lock.lock();
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ignored ->
                        orderService.getOrderByMerchantUid(merchantUid)
                                .collectList()
                                .flatMap(savedOrders -> {
                                    if (savedOrders.size() < customList.size()) {
                                        return Mono.error(new IllegalStateException("커스텀 수보다 주문 수가 적음"));
                                    }

                                    // 메뉴 이름 "커스텀" 필터링해서 매핑
                                    List<Order> customOrders = savedOrders.stream()
                                            .filter(order -> order.getMenuName().contains("커스텀"))
                                            .toList();

                                    if (customOrders.size() != customList.size()) {
                                        return Mono.error(new IllegalStateException("커스텀 수와 주문 수 불일치"));
                                    }

                                    // 빌더 기반으로 uid 세팅된 새 리스트 만들기
                                    List<CustomOrderRequestDTO> mappedList = IntStream.range(0, customList.size())
                                            .mapToObj(i -> {
                                                CustomOrderRequestDTO orig = customList.get(i);
                                                return CustomOrderRequestDTO.builder()
                                                        .uid(customOrders.get(i).getUid())
                                                        .bread(orig.getBread())
                                                        .material1(orig.getMaterial1())
                                                        .material2(orig.getMaterial2())
                                                        .material3(orig.getMaterial3())
                                                        .cheese(orig.getCheese())
                                                        .vegetable1(orig.getVegetable1())
                                                        .vegetable2(orig.getVegetable2())
                                                        .vegetable3(orig.getVegetable3())
                                                        .vegetable4(orig.getVegetable4())
                                                        .vegetable5(orig.getVegetable5())
                                                        .vegetable6(orig.getVegetable6())
                                                        .vegetable7(orig.getVegetable7())
                                                        .vegetable8(orig.getVegetable8())
                                                        .sauce1(orig.getSauce1())
                                                        .sauce2(orig.getSauce2())
                                                        .sauce3(orig.getSauce3())
                                                        .version(orig.getVersion())
                                                        .build();
                                            })
                                            .collect(Collectors.toList());

                                    return txOp.transactional(saveCustomOrders(mappedList))
                                            .thenReturn(OrderResponseDTO.builder()
                                                    .success(true)
                                                    .message("커스텀 옵션 저장 완료")
                                                    .orderUid(customOrders.get(0).getUid())
                                                    .orderUids(customOrders.stream()
                                                            .map(order -> order.getUid())
                                                            .toList())
                                                    .build());
                                })
                )
                .doFinally(signal -> {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.info("락 해제 완료: {}", lockKey);
                    }
                });
    }


    private Mono<Void> saveCustomOrders(List<CustomOrderRequestDTO> customList) {
        List<Mono<Void>> inserts = customList.stream()
                .map(custom -> {
                    if (custom.getUid() == null) {
                        throw new IllegalArgumentException("customOrderRequestDTO에 uid 누락됨");
                    }
                    CustomOrder entity = CustomOrder.builder()
                            .uid(custom.getUid()) // front에서 넘긴 orders.uid
                            .bread(custom.getBread())
                            .material1(custom.getMaterial1())
                            .material2(custom.getMaterial2())
                            .material3(custom.getMaterial3())
                            .cheese(custom.getCheese())
                            .vegetable1(custom.getVegetable1())
                            .vegetable2(custom.getVegetable2())
                            .vegetable3(custom.getVegetable3())
                            .vegetable4(custom.getVegetable4())
                            .vegetable5(custom.getVegetable5())
                            .vegetable6(custom.getVegetable6())
                            .vegetable7(custom.getVegetable7())
                            .vegetable8(custom.getVegetable8())
                            .sauce1(custom.getSauce1())
                            .sauce2(custom.getSauce2())
                            .sauce3(custom.getSauce3())
                            .version(0)
                            .build();
                    return customOrderRepository.save(entity).then();
                })
                .collect(Collectors.toList());

        return Flux.concat(inserts).then();
    }

    private boolean isNull(Integer value) {
        return value == null;
    }

}
