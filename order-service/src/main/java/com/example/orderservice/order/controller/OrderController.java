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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

        // 예약 시간이 입력되지 않았거나, 기본값(현재 시각)과 같은 경우 null 처리
        if (orderRequestDTO.getReservationDate() != null) {
            // 서버 시간 (분 단위로 자른 값)
            LocalDateTime nowTruncated = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            // 클라이언트로부터 받은 예약 시간 (분 단위로 자름)
            LocalDateTime inputTruncated = orderRequestDTO.getReservationDate().truncatedTo(ChronoUnit.MINUTES);
            // 만약 차이가 5분 미만이면, 사용자가 특별히 입력한 것이 아니라 기본값으로 간주 → null 처리
            if (Duration.between(inputTruncated, nowTruncated).abs().toMinutes() < 5) {
                orderRequestDTO.setReservationDate(null);
            }
        }

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

