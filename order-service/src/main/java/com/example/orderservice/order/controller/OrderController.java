package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.OrderService;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<OrderDetailResponseDTO> getOrders() {
        return orderService.findAllOrders()
                .collectList()
                .map(this::convertToDetailDTO)
                .switchIfEmpty(Mono.empty());
    }

    @GetMapping("/{uid}")
    public Mono<OrderDetailResponseDTO> getOrderByUid(@PathVariable Integer uid) {
        return orderService.getOrderByUid(uid)
                .map(order -> convertToDetailDTO(List.of(order)));
    }

    @GetMapping("/user/{userUid}")
    public Mono<OrderDetailResponseDTO> findAllByUserUid(@PathVariable Integer userUid) {
        return orderService.findAllByUserUid(userUid)
                .collectList()
                .map(this::convertToDetailDTO);
    }

    //결제 준비
    @PostMapping("/prepare")
    public Mono<PreparePaymentResponseDTO> preparePayment(@RequestBody PreparePaymentRequestDTO request) {
        return orderService.preparePayment(request);
    }

    @PostMapping
    public Mono<OrderResponseDTO> submitOrder(@RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        return orderService.submitOrder(orderRequestDTO)
                .collectList()
                .map(orders -> {
                    if (orders.isEmpty()) {
                        // 주문이 저장되지 않은 경우 => 실패 응답
                        return OrderResponseDTO.builder()
                                .success(false)
                                .message("주문 저장 실패")
                                .build();
                    } else {
                        // 주문이 저장된 경우 => 성공 응답
                        return OrderResponseDTO.builder()
                                .success(true)
                                .message("주문이 성공적으로 저장되었습니다. 총 " + orders.size() + "건")
                                .build();
                    }
                })
                .onErrorResume(error -> {
                    // 진짜 시스템 에러 (DB 터지거나 서버 터진 경우)
                    return Mono.just(
                            OrderResponseDTO.builder()
                                    .success(false)
                                    .message("서버 에러: " + error.getMessage())
                                    .build()
                    );
                });
    }

    //주문 상세 정보
    private OrderDetailResponseDTO convertToDetailDTO(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found");
        }

        Order firstOrder = orders.get(0);

        List<CartItem> items = orders.stream()
                .map(order -> new CartItem(
                        order.uid(),
                        order.menuName(),
                        order.amount(),
                        order.price(),
                        order.calorie()
                ))
                .toList();

        return OrderDetailResponseDTO.builder()
                .uid(firstOrder.uid())
                .userUid(firstOrder.userUid())
                .items(items)
                .payment(firstOrder.payment())
                .status(firstOrder.status().name())
                .createdDate(firstOrder.createdDate())
                .build();
    }

    @PostMapping("/update-success")
    public Mono<Void> updateOrderStatusSuccess(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToSuccess(request.getMerchantUid());
    }

    @PostMapping("/update-fail")
    public Mono<Void> updateOrderStatusFail(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToFailed(request.getMerchantUid());
    }


}

