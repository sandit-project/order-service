package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.CartItemRequestDTO;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.domain.OrderWithDelivery;
import com.example.orderservice.order.domain.StoreOrderRepository;
import com.example.orderservice.order.domain.StoreOrderResponseDTO;
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
