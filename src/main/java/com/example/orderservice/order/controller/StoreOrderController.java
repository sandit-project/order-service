package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.service.StoreOrderService;
import com.example.orderservice.order.domain.StoreOrderListResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class StoreOrderController {
    private final StoreOrderService storeOrderService;

    /**
     * 상태 변경 처리
     */
    @PutMapping("/{merchant_uid}/status")
    public Mono<Void> updateOrderStatus(@PathVariable Integer merchant_uid,
                                        @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return storeOrderService.updateStatus(merchant_uid, OrderStatus.valueOf(newStatus));
    }

    /**
     * 지점 전체 주문 수 조회
     * GET /orders/store/{storeUid}/count
     */
    @GetMapping("/store/{storeUid}/count")
    public Mono<Map<String, Long>> countByStoreUid(@PathVariable(name = "storeUid") Integer storeUid) {
        return storeOrderService.countByStoreUid(storeUid)
                .map(cnt -> Collections.singletonMap("count", cnt));
    }

}
