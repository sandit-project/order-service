package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.CartItem;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.domain.OrderResponseDTO;
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
    public Flux<OrderResponseDTO> getOrders() {
        return orderService.findAllOrders()
                .map(order -> orderService.toResponse(order));
    }

    //개별 주문 조회
    @GetMapping("/{uid}")
    public Mono<OrderResponseDTO> getOrderByUid(@PathVariable Integer uid) {
        return orderService.getOrderByUid(uid)
                .map(order -> orderService.toResponse(order));
    }

    //회원별 주문 조회
    @GetMapping("/user/{userUid}")
    public Flux<OrderResponseDTO> findAllByUserUid(Integer userUid) {
        return orderService.findAllByUserUid(userUid)
                .map(order -> orderService.toResponse(order));
    }

    //주문 승인
    @PostMapping
    public Mono<OrderResponseDTO> submitOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        for (CartItem item : orderRequestDTO.getItems()) {
            System.out.println("메뉴: " + item.menuName());
            System.out.println("수량: " + item.amount());
            System.out.println("가격: " + item.price());
        }

        System.out.println("주소: " + orderRequestDTO.getAddress());
        System.out.println("결제수단: " + orderRequestDTO.getPayment());

        return orderService.submitOrder(orderRequestDTO)
                .map(order -> orderService.toResponse(order));
    }

//    //주문 취소
//    @PatchMapping("/{uid}/cancel")
//    public Mono<Order> cancelOrder(@PathVariable Integer uid) {
//        return orderService.cancelOrder(uid);
//    }
}
