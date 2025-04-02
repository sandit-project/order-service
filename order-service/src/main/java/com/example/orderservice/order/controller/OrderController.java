package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.CartItem;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRequest;
import com.example.orderservice.order.service.OrderService;
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

    //전체 주문 조회
    @GetMapping
    public Flux<Order> getOrders() {
        return orderService.findAll();
    }

    //개별 주문 조회
    @GetMapping("/{uid}")
    public Mono<Order> getOrderByUid(Integer uid) {
        return orderService.getOrderByUid(uid);
    }

    //회원별 주문 조회
    @GetMapping("/{userUid}")
    public Flux<Order> getOrderByUserUid(Integer userUid) {
        return orderService.findAllByUserUid(userUid);
    }

    //주문 승인
    @PostMapping
    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
        for (CartItem item : orderRequest.items()) {
            System.out.println("메뉴: " + item.menuName());
            System.out.println("수량: " + item.amount());
            System.out.println("가격: " + item.price());
        }

        System.out.println("주소: " + orderRequest.address());
        System.out.println("결제수단: " + orderRequest.payment());

        return orderService.submitOrder(orderRequest);
    }

    //주문 취소
    @PatchMapping("/{uid}/cancel")
    public Mono<Order> cancelOrder(@PathVariable Integer uid) {
        return orderService.cancelOrder(uid);
    }

    //주문 상태 변경 (이건 큐로 바꿔야함)
    @PatchMapping("/{uid}/complete-order")
    public Mono<Order> completeOrder(@PathVariable Integer uid) {
        return orderService.completeOrder(uid);
    }

    @PatchMapping("/{uid}/receive")
    public Mono<Order> receiveOrder(@PathVariable Integer uid) {
        return orderService.receiveOrder(uid);
    }

    @PatchMapping("/{uid}/cooking")
    public Mono<Order> startCooking(@PathVariable Integer uid) {
        return orderService.startCooking(uid);
    }

    @PatchMapping("/{uid}/complete-cook")
    public Mono<Order> completeCooking(@PathVariable Integer uid) {
        return orderService.completeCooking(uid);
    }

    @PatchMapping("/{uid}/start-delivery")
    public Mono<Order> startDelivery(@PathVariable Integer uid) {
        return orderService.startDelivery(uid);
    }

    @PatchMapping("/{uid}/complete-delivery")
    public Mono<Order> completeDelivery(@PathVariable Integer uid) {
        return orderService.completeDelivery(uid);
    }

    @PatchMapping("/{uid}/failed")
    public Mono<Order> orderFailed(@PathVariable Integer uid) {
        return orderService.orderFailed(uid);
    }
}
