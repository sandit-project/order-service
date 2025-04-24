package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.CustomOrderService;
import com.example.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders/custom")
@RequiredArgsConstructor
public class CustomOrderController {

    private final OrderService orderService;
    private final CustomOrderService customOrderService;

    @GetMapping("/{uid}")
    public Mono<CustomOrder> getCustomOrder(@PathVariable Integer uid) {
        return customOrderService.findByUid(uid);
    }

    @PostMapping
    public Mono<OrderResponseDTO> submitCustomOrder(
            @RequestBody CustomOrderRequestDTO customOrderRequestDTO){
        return customOrderService.submitCustomOrder(customOrderRequestDTO)
                .thenReturn(OrderResponseDTO.builder()
                        .message("커스텀 주문 생성 완료!")
                        .success(true)
                        .build());
    }

    @PostMapping("/final")
    public Mono<OrderResponseDTO> submitFinalOrder(@RequestBody FinalCustomOrderRequest finalCustomOrderRequest) {
        // 1. 일반 주문 생성: OrderRequestDTO 부분으로 orders 테이블에 저장 후 uid를 가져옴
        return orderService.submitOrder(finalCustomOrderRequest.getOrderRequestDTO())
                .collectList()
                .flatMap(orders -> {
                    if (orders.isEmpty()) {
                        return Mono.just(OrderResponseDTO.builder()
                                .success(false)
                                .message("주문 저장 실패")
                                .build());
                    }
                    Integer orderUid = orders.get(0).uid();  // 생성된 주문 uid
                    // 2. 해당 uid를 이용해 커스텀 옵션과 연동하여 저장
                    return customOrderService.linkCustomOrder(orderUid, finalCustomOrderRequest.getCustomOrderRequestDTO());
                })
                .onErrorResume(e -> Mono.just(
                        OrderResponseDTO.builder()
                                .success(false)
                                .message("최종 주문 생성 실패: " + e.getMessage())
                                .build()));
    }

}
