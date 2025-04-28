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

    // 1) 옵션 프리뷰 저장
    @PostMapping
    public Mono<OrderResponseDTO> submitCustomOption(
            @RequestBody CustomOrderRequestDTO requestDTO
    ) {
        return customOrderService.submitCustomOption(requestDTO);
    }

    // 2) 최종 주문 + 옵션 연동
    @PostMapping("/final")
    public Mono<OrderResponseDTO> submitFinalOrder(
            @RequestBody FinalCustomOrderRequest finalRequestDTO
    ) {
        return customOrderService.submitFinalOrder(finalRequestDTO);
    }

    //저장된 옵션 조회
    @GetMapping("/{uid}")
    public Mono<CustomOrder> getCustomOrder(@PathVariable Integer uid) {
        return customOrderService.findByUid(uid);
    }
}
