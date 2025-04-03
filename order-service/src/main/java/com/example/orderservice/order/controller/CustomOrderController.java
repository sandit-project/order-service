package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.CustomOrderService;
import com.example.orderservice.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders/custom")
@RequiredArgsConstructor
public class CustomOrderController {

    private final OrderService orderService;
    private final CustomOrderService customOrderService;

    @PostMapping
    public Mono<Order> submitCustomOrder(@Valid @RequestBody CustomOrderRequest customOrderRequest) {
        OrderRequestDTO orderRequestDTO = customOrderRequest.getOrderRequestDTO();

        return orderService.submitOrder(orderRequestDTO)
                .flatMap(savedOrder -> {
                    CustomOrder customOrder = CustomOrder.builder()
                            .uid(savedOrder.uid()) // Order의 uid와 연동
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

                    // 커스텀 주문 DB 저장
                    return customOrderService.submitCustomOrder(customOrderRequest, savedOrder.uid(), orderRequestDTO.getItems())
                            // 커스텀 주문 저장이 끝나면 최종적으로 savedOrder 반환
                            .thenReturn(savedOrder);
                });
    }

}
