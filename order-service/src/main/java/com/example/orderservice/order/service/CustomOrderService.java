package com.example.orderservice.order.service;

import com.example.orderservice.cart.CustomCartRepository;
import com.example.orderservice.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;

    public Mono<OrderResponseDTO> submitCustomOrder(CustomOrderRequest customOrderRequest) {
        CustomOrder customOrder = CustomOrder.builder()
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

        return customOrderRepository.save(customOrder)
                .then(orderService.submitOrder(customOrderRequest.getOrderRequestDTO())
                        .collectList()
                        .map(orders -> OrderResponseDTO.builder()
                                .success(true)
                                .message("커스텀 저장 및 주문이 성공적으로 완료되었습니다. 총 " + orders.size() + "건")
                                .build())
                )
                .onErrorResume(error -> Mono.just(
                        OrderResponseDTO.builder()
                                .success(false)
                                .message("커스텀+주문 실패: " + error.getMessage())
                                .build()
                ));
    }
}
