package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.*;
import com.example.orderservice.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class CustomOrderServiceTest {

    @Mock
    private CustomOrderRepository customOrderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private TransactionalOperator txOp;

    @InjectMocks
    private CustomOrderService customOrderService;

    @BeforeEach
    void 설정() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 커스텀옵션이_없는경우_저장없이_성공응답() {
        OrderRequestDTO dummyOrder = OrderRequestDTO.builder()
                .merchantUid("no-custom-uid")
                .build();

        FinalCustomOrderRequest request = FinalCustomOrderRequest.builder()
                .orderRequestDTO(dummyOrder)
                .customOrderRequestDTO(Collections.emptyList())
                .build();

        when(orderService.getOrderByMerchantUid(anyString()))
                .thenReturn(Flux.just(Order.builder()
                        .uid(1)
                        .menuName("테스트 메뉴")
                        .price(1000)
                        .userUid(1)
                        .storeUid(1)
                        .merchantUid("no-custom-uid")
                        .status(OrderStatus.PAYMENT_COMPLETED)
                        .payment("card")
                        .amount(1)
                        .createdDate(LocalDateTime.now())
                        .calorie(100.0)
                        .reservationDate(LocalDateTime.now())
                        .build()
                ));

        StepVerifier.create(customOrderService.submitFinalOrder(request))
                .expectNextMatches(res ->
                        res.isSuccess() &&
                                res.getMessage().equals("커스텀 옵션 저장 완료") &&
                                res.getOrderUid() != null &&
                                res.getOrderUids() != null
                )
                .verifyComplete();

        verifyNoInteractions(customOrderRepository);
    }

    @Test
    void 커스텀옵션이_존재할_경우_DB저장_후_성공응답() {
        CustomOrderRequestDTO dto = CustomOrderRequestDTO.builder()
                .uid(123)
                .bread(3)
                .material1(1)
                .cheese(1)
                .vegetable1(1)
                .sauce1(1)
                .build();

        OrderRequestDTO dummyOrder = OrderRequestDTO.builder()
                .merchantUid("custom-uid")
                .build();

        FinalCustomOrderRequest request = FinalCustomOrderRequest.builder()
                .orderRequestDTO(dummyOrder)
                .customOrderRequestDTO(List.of(dto))
                .build();

        when(customOrderRepository.save(any(CustomOrder.class)))
                .thenReturn(Mono.just(CustomOrder.builder().uid(123).bread(1).build()));

        when(txOp.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderService.getOrderByMerchantUid("custom-uid"))
                .thenReturn(Flux.just(Order.builder()
                        .uid(123)
                        .menuName("테스트 메뉴")
                        .price(1000)
                        .userUid(1)
                        .storeUid(1)
                        .merchantUid("custom-uid")
                        .status(OrderStatus.PAYMENT_COMPLETED)
                        .payment("card")
                        .amount(1)
                        .createdDate(LocalDateTime.now())
                        .calorie(100.0)
                        .reservationDate(LocalDateTime.now())
                        .build()));

        StepVerifier.create(customOrderService.submitFinalOrder(request))
                .expectNextMatches(res ->
                        res.isSuccess() &&
                                res.getMessage().equals("커스텀 옵션 저장 완료") &&
                                res.getOrderUid() == 123 &&
                                res.getOrderUids().contains(123)
                )
                .verifyComplete();

        verify(customOrderRepository, times(1)).save(any(CustomOrder.class));
    }


    @Test
    void 전체_주문_조회() {
        CustomOrder mockOrder = CustomOrder.builder().uid(1).bread(2).build();
        when(customOrderRepository.findAll()).thenReturn(Flux.just(mockOrder));

        StepVerifier.create(customOrderService.findAllOrders())
                .expectNext(mockOrder)
                .verifyComplete();
    }

    @Test
    void 주문_uid_조회() {
        CustomOrder mockOrder = CustomOrder.builder().uid(42).bread(1).build();
        when(customOrderRepository.findById(42)).thenReturn(Mono.just(mockOrder));

        StepVerifier.create(customOrderService.findByUid(42))
                .expectNext(mockOrder)
                .verifyComplete();
    }
}
