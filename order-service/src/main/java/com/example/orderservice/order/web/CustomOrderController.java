package com.example.orderservice.order.web;

import com.example.orderservice.order.domain.CustomOrder;
import com.example.orderservice.order.domain.CustomOrderService;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderService;
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
        // 1. 일반 주문 생성: 결제, 수량 등 기본 로직 처리
        Order order = Order.builder()
                .userUid(customOrderRequest.userUid())
                .socialUid(customOrderRequest.socialUid())
                .menuName(customOrderRequest.menuName())
                .payment(customOrderRequest.payment())
                .build();

        // 2. 일반 주문이 성공하면, 커스텀 주문 정보 저장 (Order의 uid와 연관)
        return orderService.submitOrder(order)
                .flatMap(savedOrder -> {
                    CustomOrder customOrder = CustomOrder.builder()
                        .uid(savedOrder.uid())
                        .bread(customOrderRequest.bread())
                        .material1(customOrderRequest.material1())
                        .material2(customOrderRequest.material2())
                        .material3(customOrderRequest.material3())
                        .build();

                    return customOrderService.submitCustomOrder(customOrderRequest, order.uid(), customOrderRequest.menuUid())
                            .thenReturn(savedOrder);

                });
    }
}
