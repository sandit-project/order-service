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
//    @PostMapping
//    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
//        Order order = Order.builder()
//                .userUid(orderRequest.userUid())
//                .socialUid(orderRequest.socialUid())
//                .menuName(orderRequest.menuName())
//                .amount(orderRequest.amount())
//                .payment(orderRequest.payment())
//                .build();
//        return orderService.submitOrder(order);
//    }

    @PostMapping
    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.submitOrder(orderRequest);
    }

    //주문 취소
    @PatchMapping("/{uid}/cancel")
    public Mono<Order> cancelOrder(@PathVariable Integer uid) {
        return orderService.cancelOrder(uid);
    }

    //주문 상태 변경
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
