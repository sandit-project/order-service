package com.example.orderservice.order.web;

import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Flux<Order> getOrders() {
        return orderService.findAll();
    }

    @PostMapping
    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Order order = Order.builder()
                .userUid(orderRequest.userUid())
                .socialUid(orderRequest.socialUid())
                .menuUid(orderRequest.menuUid())
                .menuName(orderRequest.menuName())
                .amount(orderRequest.amount())
                .payment(orderRequest.payment())
                .build();
        return orderService.submitOrder(order);
    }

    @PatchMapping("/{uid}/cancel")
    public Mono<Order> cancelOrder(@PathVariable Integer uid) {
        return orderService.cancelOrder(uid);
    }
}
