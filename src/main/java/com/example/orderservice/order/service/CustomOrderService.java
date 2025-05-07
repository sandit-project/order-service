package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;
    private final TransactionalOperator txOp;

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
            return Mono.error(new IllegalArgumentException("커스텀 옵션이 없습니다."));
        }

        return txOp.transactional(saveCustomOrders(customList))
                .then(Mono.just(OrderResponseDTO.builder()
                        .success(true)
                        .message("커스텀 옵션 저장 완료")
                        .build()));
    }

    private Mono<Void> saveCustomOrders(List<CustomOrderRequestDTO> customList) {
        List<Mono<Void>> inserts = customList.stream()
                .map(custom -> {
                    CustomOrder entity = CustomOrder.builder()
                            .uid(custom.getUid())      // 여기 custom.getUid() 는 front에서 넘긴 orders.uid (한 번만 insert 된)
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
