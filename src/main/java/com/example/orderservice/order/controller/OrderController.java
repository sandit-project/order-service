package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.OrderService;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
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
        log.info("orderRequestDTO: {}", orderRequestDTO);
        return orderService.submitOrder(orderRequestDTO);
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
                .merchantUid(firstOrder.merchantUid())
                .payment(firstOrder.payment())
                .status(firstOrder.status().name())
                .createdDate(firstOrder.createdDate())
                .reservationDate(firstOrder.reservationDate())
                .build();
    }

    @PostMapping("/update-success")
    public Mono<OrderResponseDTO> updateOrderStatusSuccess(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToSuccess(request.getMerchantUid())
                .thenReturn(OrderResponseDTO.builder()
                        .success(true)
                        .message("주문 완료!")
                        .build());
    }

    @PostMapping("/update-fail")
    public Mono<OrderResponseDTO> updateOrderStatusFail(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToFailed(request.getMerchantUid())
                .thenReturn(OrderResponseDTO.builder()
                        .success(false)
                        .message("주문 실패!")
                        .build());
    }

}

