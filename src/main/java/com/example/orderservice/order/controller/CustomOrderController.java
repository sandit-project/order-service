package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.CustomOrderService;
import com.example.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/orders/custom")
@RequiredArgsConstructor
public class CustomOrderController {

    private final OrderService orderService;
    private final CustomOrderService customOrderService;

    @PostMapping("/final")
    public Mono<OrderResponseDTO> submitFinalOrder(
            @RequestBody FinalCustomOrderRequest finalRequestDTO
    ) {
        log.info("submit Final custom order: {}", finalRequestDTO);
        return customOrderService.submitFinalOrder(finalRequestDTO);
    }

    //저장된 옵션 조회
    @GetMapping("/{uid}")
    public Mono<CustomOrder> getCustomOrder(@PathVariable Integer uid) {
        return customOrderService.findByUid(uid);
    }
}
