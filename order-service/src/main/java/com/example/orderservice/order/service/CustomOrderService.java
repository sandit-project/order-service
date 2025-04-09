package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.CustomOrder;
import com.example.orderservice.order.domain.CustomOrderRepository;
import com.example.orderservice.order.domain.CustomOrderRequest;
import com.example.orderservice.order.domain.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;

    public Mono<OrderResponseDTO> submitCustomOrder(CustomOrderRequest customOrderRequest) {
        // 1. 먼저 공통 주문 저장
        return orderService.submitOrder(customOrderRequest.getOrderRequestDTO())
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
                            .bread(customOrderRequest.getBread())
                            .material1(customOrderRequest.getMaterial1())
                            .material2(customOrderRequest.getMaterial2())
                            .material3(customOrderRequest.getMaterial3())
                            .cheese(customOrderRequest.getCheese())
                            .vegetable1(customOrderRequest.getVegetable1())
                            .vegetable2(customOrderRequest.getVegetable2())
                            .vegetable3(customOrderRequest.getVegetable3())
                            .vegetable4(customOrderRequest.getVegetable4())
                            .vegetable5(customOrderRequest.getVegetable5())
                            .vegetable6(customOrderRequest.getVegetable6())
                            .vegetable7(customOrderRequest.getVegetable7())
                            .vegetable8(customOrderRequest.getVegetable8())
                            .sauce1(customOrderRequest.getSauce1())
                            .sauce2(customOrderRequest.getSauce2())
                            .sauce3(customOrderRequest.getSauce3())
                            .build();

                    // 4. custom_order 저장
                    return customOrderRepository.save(customOrder)
                            .thenReturn(OrderResponseDTO.builder()
                                    .success(true)
                                    .message("커스텀 주문 성공")
                                    .build());
                });
    }



}
