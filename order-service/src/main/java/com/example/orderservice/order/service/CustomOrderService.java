package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;

    public Flux<CustomOrder> findAllOrders() {
        return customOrderRepository.findAll();
    }

    public Mono<CustomOrder> findByUid(Integer uid) {
        return customOrderRepository.findById(uid);
    }

    public Mono<OrderResponseDTO> submitCustomOrder(CustomOrderRequestDTO customOrderRequestDTO) {
        // 0. 방어 로직 추가 (필수값 체크)
        if (isNull(customOrderRequestDTO.getBread()) ||
                isNull(customOrderRequestDTO.getMaterial1()) ||
                isNull(customOrderRequestDTO.getVegetable1()) ||
                isNull(customOrderRequestDTO.getSauce1())) {

            return Mono.just(OrderResponseDTO.builder()
                    .success(false)
                    .message("필수 커스텀 정보 누락: bread, material1, vegetable1, sauce1은 필수입니다.")
                    .build());
        }

        customOrderRequestDTO.getOrderRequestDTO()
                .getItems()
                .replaceAll(item -> new CartItem(
                        item.cartUid(),
                        "커스텀 샌드위치",
                        item.amount(),
                        item.price(),
                        item.calorie()
                ));

        // 1. 먼저 공통 주문 저장
        return orderService.submitOrder(customOrderRequestDTO.getOrderRequestDTO())
                .collectList()
                .flatMap(orders -> {
                    if (orders.isEmpty()) {
                        return Mono.just(OrderResponseDTO.builder()
                                .success(false)
                                .message("커스텀 주문 후 공통 주문 실패")
                                .build());
                    }

                    // 2. 저장된 주문(Order)에서 uid를 가져온다
                    Integer orderUid = orders.get(0).uid();

                    // 3. 그 uid를 CustomOrder에 세팅
                    CustomOrder customOrder = CustomOrder.builder()
                            .uid(orderUid)
                            .bread(customOrderRequestDTO.getBread())
                            .material1(customOrderRequestDTO.getMaterial1())
                            .material2(customOrderRequestDTO.getMaterial2())
                            .material3(customOrderRequestDTO.getMaterial3())
                            .cheese(customOrderRequestDTO.getCheese())
                            .vegetable1(customOrderRequestDTO.getVegetable1())
                            .vegetable2(customOrderRequestDTO.getVegetable2())
                            .vegetable3(customOrderRequestDTO.getVegetable3())
                            .vegetable4(customOrderRequestDTO.getVegetable4())
                            .vegetable5(customOrderRequestDTO.getVegetable5())
                            .vegetable6(customOrderRequestDTO.getVegetable6())
                            .vegetable7(customOrderRequestDTO.getVegetable7())
                            .vegetable8(customOrderRequestDTO.getVegetable8())
                            .sauce1(customOrderRequestDTO.getSauce1())
                            .sauce2(customOrderRequestDTO.getSauce2())
                            .sauce3(customOrderRequestDTO.getSauce3())
                            .build();

                    // 4. custom_order 저장
                    return customOrderRepository.save(customOrder)
                            .thenReturn(OrderResponseDTO.builder()
                                    .success(true)
                                    .message("커스텀 주문 성공")
                                    .build());
                });
    }

    private boolean isNull(Integer value) {
        return value == null;
    }
}
