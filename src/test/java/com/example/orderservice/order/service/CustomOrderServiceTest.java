package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.CustomOrder;
import com.example.orderservice.order.domain.CustomOrderRepository;
import com.example.orderservice.order.domain.FinalCustomOrderRequest;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOrderServiceTest {

    @Mock private CustomOrderRepository customOrderRepository;
    @Mock private PaymentService paymentService;
    @Mock private OrderService orderService;
    @Mock private TransactionalOperator txOp;
    @InjectMocks private CustomOrderService customOrderService;

    @BeforeEach
    void setUp() {
        // Mono transactional pass-through
        Mockito.lenient()
                .when(txOp.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 모든_커스텀_주문을_조회한다() {
        CustomOrder o1 = CustomOrder.builder().uid(1).build();
        CustomOrder o2 = CustomOrder.builder().uid(2).build();
        when(customOrderRepository.findAll())
                .thenReturn(Flux.just(o1, o2));

        StepVerifier.create(customOrderService.findAllOrders())
                .expectNext(o1, o2)
                .verifyComplete();
    }

    @Test
    void UID로_커스텀_주문을_조회한다() {
        CustomOrder o = CustomOrder.builder().uid(5).build();
        when(customOrderRepository.findById(5))
                .thenReturn(Mono.just(o));

        StepVerifier.create(customOrderService.findByUid(5))
                .expectNext(o)
                .verifyComplete();
    }

    @Test
    void 커스텀_리스트_없으면_주문응답_즉시_반환한다() {
        FinalCustomOrderRequest req = mock(FinalCustomOrderRequest.class);
        when(req.getCustomOrderRequestDTO()).thenReturn(null);

        OrderRequestDTO or = mock(OrderRequestDTO.class);
        when(req.getOrderRequestDTO()).thenReturn(or);
        when(or.getMerchantUid()).thenReturn("mid");

        // ► model.Order 인스턴스를 만들어야 합니다.
        Order ord = Order.builder()
                .uid(10)
                .build();

        when(orderService.getOrderByMerchantUid("mid"))
                .thenReturn(Flux.just(ord));

        StepVerifier.create(customOrderService.submitFinalOrder(req))
                .assertNext(dto -> {
                    assertTrue(dto.isSuccess());
                    assertEquals(10, dto.getOrderUid());
                })
                .verifyComplete();
    }
}
