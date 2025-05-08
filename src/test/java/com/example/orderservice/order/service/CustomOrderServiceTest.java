package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        FinalCustomOrderRequest request = FinalCustomOrderRequest.builder()
                .customOrderRequestDTO(Collections.emptyList())
                .build();

        StepVerifier.create(customOrderService.submitFinalOrder(request))
                .expectNextMatches(res -> res.isSuccess() && res.getMessage().equals("커스텀 옵션 저장 완료"))
                .verifyComplete();

        verifyNoInteractions(customOrderRepository);
    }

    @Test
    void 커스텀옵션이_존재할_경우_DB저장_후_성공응답() {
        CustomOrderRequestDTO dto = CustomOrderRequestDTO.builder()
                .uid(123)
                .bread(3) // ← 여긴 enum으로 바꿀 여지 있음
                .build();

        FinalCustomOrderRequest request = FinalCustomOrderRequest.builder()
                .customOrderRequestDTO(List.of(dto))
                .build();

        when(customOrderRepository.save(any(CustomOrder.class)))
                .thenReturn(Mono.just(CustomOrder.builder()
                        .uid(123)
                        .bread(1)
                        .material1(2)     // 필수
                        .cheese(1)    // 필수
                        .vegetable1(2)      // 필수
                        .sauce1(1)     // 필수
                        .build()));
        when(txOp.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(customOrderService.submitFinalOrder(request))
                .expectNextMatches(res -> res.isSuccess() && res.getMessage().equals("커스텀 옵션 저장 완료"))
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
