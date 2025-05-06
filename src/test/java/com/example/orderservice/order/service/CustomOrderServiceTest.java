//package com.example.orderservice.order.service;
//
//import com.example.orderservice.order.domain.*;
//import com.example.orderservice.order.service.CustomOrderService;
//import com.example.orderservice.order.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//class CustomOrderServiceTest {
//
//    private CustomOrderRepository customOrderRepository;
//    private OrderService orderService;
//    private CustomOrderService customOrderService;
//
//    @BeforeEach
//    void setUp() {
//        customOrderRepository = mock(CustomOrderRepository.class);
//        orderService = mock(OrderService.class);
//        customOrderService = new CustomOrderService(customOrderRepository, orderService);
//    }
//
//    @Test
//    void 필수값_누락시_에러반환() {
//        CustomOrderRequestDTO invalidRequest = CustomOrderRequestDTO.builder()
//                .bread(null)
//                .material1(null)
//                .vegetable1(null)
//                .sauce1(null)
//                .build();
//
//        Mono<OrderResponseDTO> responseMono = customOrderService.submitCustomOrder(invalidRequest);
//        StepVerifier.create(responseMono)
//                .assertNext(response -> {
//                    assertFalse(response.isSuccess());
//                    assertEquals("필수 커스텀 정보 누락: bread, material1, vegetable1, sauce1은 필수입니다.", response.getMessage());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void 정상_커스텀주문_성공() {
//        // 내부적으로 주문 생성은 orderService.submitOrder()를 호출하지만,
//        // 키오스크 방식에서는 커스텀 옵션만 저장하므로, orderRequestDTO는 사용하지 않음.
//        CustomOrderRequestDTO request = CustomOrderRequestDTO.builder()
//                .bread(1)
//                .material1(2)
//                .vegetable1(3)
//                .sauce1(4)
//                .build();
//
//        when(customOrderRepository.save(any(CustomOrder.class)))
//                .thenReturn(Mono.just(new CustomOrder(1, 1, 2, 3, null, null, null, null, null, null, null, null, null, null, 4, null, null, 1)));
//
//        Mono<OrderResponseDTO> responseMono = customOrderService.submitCustomOrder(request);
//        StepVerifier.create(responseMono)
//                .assertNext(response -> {
//                    assertTrue(response.isSuccess());
//                    assertEquals("커스텀 주문 옵션 저장 성공. (추후 주문 생성 시 연동 필요)", response.getMessage());
//                })
//                .verifyComplete();
//    }
//}