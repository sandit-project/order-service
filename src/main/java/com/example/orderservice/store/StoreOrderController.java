package com.example.orderservice.store;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class StoreOrderController {
    private final StoreOrderService storeOrderService;

    /**
     * 지점 주문 목록 조회 API
     * GET /store-orders/{storeUid}?limit=10&lastUid=123
     */

    @GetMapping("/store/{storeUid}")
    public Mono<StoreOrderListResponseDTO> getAllOrdersByStoreUid(@PathVariable(name = "storeUid") Integer storeUid,
                                                                  @RequestParam(name = "limit", defaultValue = "10") int limit,
                                                                  @RequestParam(name="lastUid",required = false) Integer lastUid)
    {

        return storeOrderService.findAllByStoreUid(storeUid, limit, lastUid)
                .collectList()
                .map(list->{
                    boolean lastPage = list.size() <limit;
                    Integer nextCursor = lastPage ? null : list.get(list.size()-1).getUid();
                    StoreOrderListResponseDTO test =  StoreOrderListResponseDTO.builder()
                            .storeOrderLists(list)
                            .lastPage(lastPage)
                            .nextCursor(nextCursor)
                            .build();
                    return test;
                });
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
