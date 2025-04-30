package com.example.orderservice.store;

import com.example.orderservice.order.domain.CartItem;
import com.example.orderservice.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreOrderService {
    private final StoreOrderRepository storeOrderRepository;

    /**
     * 지점 주문 목록 조회
     */
    public Flux<StoreOrderResponseDTO> findAllByStoreUid (Integer storeUid, int limit, Integer lastUid){
        Flux<Order> storeOrders = (lastUid !=null)
                ? storeOrderRepository.findOrderByStoreUidWithCursor(storeUid,lastUid,limit)
                : storeOrderRepository.findOrderByStoreUid(storeUid,limit);

        Flux<StoreOrderResponseDTO> test =  storeOrders.map(order->StoreOrderResponseDTO.builder()
                .uid(order.uid())
                .userUid(order.userUid())
                .storeUid(order.storeUid())
                .merchantUid(order.merchantUid())
                .items(List.of(new CartItem(
                        order.uid(),
                        order.menuName(),
                        order.amount(),
                        order.price(),
                        order.calorie(),
                        order.version()
                )))
                .payment(order.payment())
                .status(order.status().name())
                .createdDate(order.createdDate())
                .reservationDate(order.reservationDate())
                .build()
        );
        return test;
    }

    /**
     * 지점 전체 주문 수 조회
     */
    public Mono<Long> countByStoreUid(Integer storeUid) {
        return storeOrderRepository.findAllOrders(storeUid).count();
    }
}
