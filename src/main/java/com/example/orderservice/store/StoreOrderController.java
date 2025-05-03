package com.example.orderservice.store;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
     * 지점 주문 목록 조회 API
     * GET /store-orders/{storeUid}?limit=10&lastUid=123
     */

    @GetMapping("/store/{storeUid}")
    public Mono<StoreOrderListResponseDTO> getAllOrdersByStoreUid(@PathVariable(name = "storeUid") Integer storeUid,
                                                                  @RequestParam(name = "limit", defaultValue = "10") int limit,
                                                                  @RequestParam(name = "lastUid", required = false) Integer lastUid,
                                                                  @RequestParam(name = "status", required = false) String status) {
        log.info("get all orders by storeUid: {}, limit: {}, lastUid: {}, status: {}", storeUid, limit, lastUid, status);

        Mono<StoreOrderListResponseDTO> test = storeOrderService.findAllByStoreUid(storeUid, limit, lastUid, status)
                .collectList()
                .map(list -> {
                    boolean lastPage = list.size() < limit;
                    Integer nextCursor = lastPage ? null : list.get(list.size() - 1).getUid();
                    StoreOrderListResponseDTO orderLists = StoreOrderListResponseDTO.builder()
                            .storeOrderLists(list)
                            .lastPage(lastPage)
                            .nextCursor(nextCursor)
                            .build();
                    return orderLists;
                });
        log.info("test: {}", test);
        return test;
    }

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
