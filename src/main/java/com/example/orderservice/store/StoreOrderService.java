package com.example.orderservice.store;

import com.example.orderservice.order.domain.CartItemRequestDTO;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderStatus;
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
    public Flux<StoreOrderResponseDTO> findAllByStoreUid (
            Integer storeUid, int limit, Integer lastUid,String status)
    {
        Flux<Order> storeOrders;
        if (status != null) {
            OrderStatus os = OrderStatus.valueOf(status);
            storeOrders = (lastUid != null)
                    ? storeOrderRepository.findOrderByStoreUidAndStatusWithCursor(storeUid, os, lastUid, limit)
                    : storeOrderRepository.findOrderByStoreUidAndStatus(storeUid, os, limit);
        } else {
            storeOrders = (lastUid != null)
                    ? storeOrderRepository.findOrderByStoreUidWithCursor(storeUid, lastUid, limit)
                    : storeOrderRepository.findOrderByStoreUid(storeUid, limit);
        }

        return storeOrders.map(order->StoreOrderResponseDTO.builder()
                .uid(order.uid())
                .userUid(order.userUid())
                .storeUid(order.storeUid())
                .merchantUid(order.merchantUid())
                .items(List.of(new CartItemRequestDTO(
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

    }

    /**
     * 상태 변경 로직
     */
    public Mono<Void> updateStatus(Integer merchantUid, OrderStatus newStatus) {
        return storeOrderRepository.updateStatusByUid(merchantUid,newStatus);
    }

    /**
     * 지점 전체 주문 수 조회
     */
    public Mono<Long> countByStoreUid(Integer storeUid) {
        return storeOrderRepository.findAllOrders(storeUid).count();
    }
}
