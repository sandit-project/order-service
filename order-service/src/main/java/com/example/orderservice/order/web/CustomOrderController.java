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
        // 1. 일반 주문용 OrderRequest 생성
        OrderRequest orderRequest = new OrderRequest(
                customOrderRequest.userUid(),
                customOrderRequest.socialUid(),
                customOrderRequest.menuName(),
                customOrderRequest.amount(),
                5000, // 예시: 가격(Price). 실제 로직에 맞게 세팅
                0.0,  // 예시: 칼로리(Calorie). 필요하다면 계산해서 넣기
                customOrderRequest.payment(),
                "merchant_" + System.currentTimeMillis() // 임의로 merchantUid 생성 예시
        );

        // 2. 일반 주문(결제 등) 처리
        return orderService.submitOrder(orderRequest)
                .flatMap(savedOrder -> {
                    // 3. 일반 주문이 성공하면, 커스텀 주문 정보 저장
                    CustomOrder customOrder = CustomOrder.builder()
                            .uid(savedOrder.uid()) // Order의 uid와 연동
                            .bread(customOrderRequest.bread())
                            .material1(customOrderRequest.material1())
                            .material2(customOrderRequest.material2())
                            .material3(customOrderRequest.material3())
                            .cheese(customOrderRequest.cheese())
                            .vegetable1(customOrderRequest.vegetable1())
                            .vegetable2(customOrderRequest.vegetable2())
                            .vegetable3(customOrderRequest.vegetable3())
                            .vegetable4(customOrderRequest.vegetable4())
                            .vegetable5(customOrderRequest.vegetable5())
                            .vegetable6(customOrderRequest.vegetable6())
                            .vegetable7(customOrderRequest.vegetable7())
                            .vegetable8(customOrderRequest.vegetable8())
                            .sauce1(customOrderRequest.sauce1())
                            .sauce2(customOrderRequest.sauce2())
                            .sauce3(customOrderRequest.sauce3())
                            .build();

                    // 커스텀 주문 DB 저장
                    return customOrderService.submitCustomOrder(customOrderRequest, savedOrder.uid(), customOrderRequest.menuUid())
                            // 커스텀 주문 저장이 끝나면 최종적으로 savedOrder 반환
                            .thenReturn(savedOrder);
                });
    }

}
