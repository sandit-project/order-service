package com.example.orderservice.order.controller;

import com.example.orderservice.order.service.OrderService;
import com.example.orderservice.payment.CancelPaymentRequestDTO;
import com.example.orderservice.payment.CancelPaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/payments")
public class PaymentsController {

    private final OrderService orderService;

    // 1) 취소 준비: 이전 상태 저장 & DB → CANCELLED
    @PostMapping("/init")
    public Mono<CancelPaymentResponseDTO> initCancel(@RequestBody CancelPaymentRequestDTO dto) {
        return orderService.initCancel(dto.getMerchantUid());
    }

    // 2) 취소 확정: Redis 키 삭제
    @PostMapping("/confirm")
    public Mono<CancelPaymentResponseDTO> confirmCancel(@RequestBody CancelPaymentRequestDTO dto) {
        return orderService.confirmCancel(dto.getMerchantUid());
    }

    // 3) 보상 트랜잭션: Redis 에 남아 있던 이전 상태로 롤백
    @PostMapping("/compensate")
    public Mono<CancelPaymentResponseDTO> compensateCancel(@RequestBody CancelPaymentRequestDTO dto) {
        return orderService.compensateCancel(dto.getMerchantUid());
    }
}
