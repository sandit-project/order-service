package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.DeliveryOrderResponseDTO;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.domain.DeliveryOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testGetStoreOrders_withExplicitStatus() {
        // given
        Integer storeUid = 1;
        String status = OrderStatus.COOKING.name();  // 예시 status
        DeliveryOrderResponseDTO dto = new DeliveryOrderResponseDTO();
        dto.setMerchantUid("m1");
        dto.setStoreUid(Long.valueOf(storeUid));
        dto.setStatus(status);

        when(deliveryOrderRepository.getStoreOrdersByStatusAndStoreUid(storeUid, status))
                .thenReturn(Flux.just(dto));

        // when
        Flux<DeliveryOrderResponseDTO> result = orderService.getStoreOrders(storeUid, status);

        // then
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(deliveryOrderRepository)
                .getStoreOrdersByStatusAndStoreUid(storeUid, status);
    }

    @Test
    void testGetStoreOrders_withNullStatus_defaultsToPaymentCompleted() {
        // given
        Integer storeUid = 2;
        String status = null;
        String defaultStatus = OrderStatus.PAYMENT_COMPLETED.name();
        DeliveryOrderResponseDTO dto = new DeliveryOrderResponseDTO();
        dto.setMerchantUid("m2");
        dto.setStoreUid(Long.valueOf(storeUid));
        dto.setStatus(defaultStatus);

        when(deliveryOrderRepository.getStoreOrdersByStatusAndStoreUid(storeUid, defaultStatus))
                .thenReturn(Flux.just(dto));

        // when
        Flux<DeliveryOrderResponseDTO> result = orderService.getStoreOrders(storeUid, status);

        // then
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(deliveryOrderRepository)
                .getStoreOrdersByStatusAndStoreUid(storeUid, defaultStatus);
    }
}
