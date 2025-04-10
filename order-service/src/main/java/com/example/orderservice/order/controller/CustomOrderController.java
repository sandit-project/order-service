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
        return customOrderService.submitCustomOrder(customOrderRequestDTO);
    }

}
