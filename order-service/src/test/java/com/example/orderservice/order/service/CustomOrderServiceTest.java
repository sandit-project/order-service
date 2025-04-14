package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.service.CustomOrderService;
import com.example.orderservice.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.orderservice.order.domain.OrderStatus.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomOrderServiceTest {

    @Mock
    private CustomOrderRepository customOrderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CustomOrderService customOrderService;

    @BeforeEach
    void setUp() {
        customOrderRepository = mock(CustomOrderRepository.class);
        orderService = mock(OrderService.class);
        customOrderService = new CustomOrderService(customOrderRepository, orderService);
    }

    @Test
    void 필수값_누락시_에러반환() {
        CustomOrderRequestDTO invalidRequest = CustomOrderRequestDTO.builder()
                .bread(null)  // 빵이 null
                .material1(null)
                .vegetable1(null)
                .sauce1(null)
                .build();

        Mono<OrderResponseDTO> responseMono = customOrderService.submitCustomOrder(invalidRequest);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertFalse(response.isSuccess());
                    assertEquals("필수 커스텀 정보 누락: bread, material1, vegetable1, sauce1은 필수입니다.", response.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void 정상_커스텀주문_성공() {
        OrderRequestDTO innerRequest = OrderRequestDTO.builder()
                .userUid(1)
                .storeUid(2)
                .items(List.of(new CartItem(1, "커스텀 테스트 샌드위치", 1, 5000, 300.0)))
                .payment("카드")
                .merchantUid("test-merchant")
                .paymentSuccess(true)
                .reservationDate(LocalDateTime.now())
                .build();

        CustomOrderRequestDTO request = CustomOrderRequestDTO.builder()
                .bread(1)
                .material1(2)
                .vegetable1(3)
                .sauce1(4)
                .orderRequestDTO(innerRequest)
                .build();


        when(orderService.submitOrder(any()))
                .thenReturn(Flux.just(new com.example.orderservice.order.domain.Order(1, 1, 2, 3, "merchant-1234", "커스텀 샌드위치", 1, 6000, 300.0, "카드", ORDER_CREATED, LocalDateTime.now(),LocalDateTime.now(),1)));

        when(customOrderRepository.save(any(CustomOrder.class)))
                .thenReturn(Mono.just(new CustomOrder(1,1,1,2,3,1,1,2, 3, 4, 5, 6, 7, 8, 1, 2, 3,1)));

        Mono<OrderResponseDTO> responseMono = customOrderService.submitCustomOrder(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("커스텀 주문 성공", response.getMessage());
                })
                .verifyComplete();
    }
}