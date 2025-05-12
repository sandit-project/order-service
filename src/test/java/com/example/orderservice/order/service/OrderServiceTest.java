package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.payment.PaymentService;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private DeliveryOrderRepository deliveryOrderRepository;
    @Mock private DeliveryAddressRepository deliveryAddressRepository;
    @Mock private PaymentService paymentService;
    @Mock private StreamBridge streamBridge;
    @Mock private TransactionalOperator txOp;
    @InjectMocks private OrderService orderService;

    @BeforeEach
    void setUp() {
        Mockito.lenient()
                .when(txOp.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 모든_주문을_조회한다() {
        Order o1 = Order.builder().uid(1).build();
        Order o2 = Order.builder().uid(2).build();
        when(orderRepository.findAllOrders())
                .thenReturn(Flux.just(o1, o2));

        StepVerifier.create(orderService.findAllOrders())
                .expectNext(o1, o2)
                .verifyComplete();
    }

    @Test
    void 조리중_주문을_조회한다() {
        DeliveryOrderResponseDTO dto = Mockito.mock(DeliveryOrderResponseDTO.class);
        when(deliveryOrderRepository.getCookingOrders())
                .thenReturn(Flux.just(dto));

        StepVerifier.create(orderService.getCookingOrders())
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void 배달중_주문을_조회한다() {
        DeliveryOrderResponseDTO dto = Mockito.mock(DeliveryOrderResponseDTO.class);
        when(deliveryOrderRepository.getDeliveringOrders())
                .thenReturn(Flux.just(dto));

        StepVerifier.create(orderService.getDeliveringOrders())
                .expectNext(dto)
                .verifyComplete();
    }

    @Test
    void UID로_주문을_조회한다() {
        Order o = Order.builder().uid(3).build();
        when(orderRepository.findById(3))
                .thenReturn(Mono.just(o));

        StepVerifier.create(orderService.getOrderByUid(3))
                .expectNext(o)
                .verifyComplete();
    }

    @Test
    void merchantUid로_주문을_조회한다() {
        Order o = Order.builder().merchantUid("x").build();
        when(orderRepository.findByMerchantUid("x"))
                .thenReturn(Flux.just(o));

        StepVerifier.create(orderService.getOrderByMerchantUid("x"))
                .expectNext(o)
                .verifyComplete();
    }

    @Test
    void 사용자_UID로_주문을_조회한다() {
        Order o = Order.builder().userUid(5).build();
        when(orderRepository.findByUserUid(5))
                .thenReturn(Flux.just(o));

        StepVerifier.create(orderService.findAllByUserUid(5))
                .expectNext(o)
                .verifyComplete();
    }

    @Test
    void 사전_결제_준비_성공() {
        PreparePaymentRequestDTO req = Mockito.mock(PreparePaymentRequestDTO.class);
        when(req.getMerchantUid()).thenReturn("mu");
        when(req.getStoreUid()).thenReturn(1);
        when(req.getMenuName()).thenReturn("menu");
        when(req.getTotalPrice()).thenReturn(500);
        when(req.getUserUid()).thenReturn(2);
        when(req.getReservationDate()).thenReturn(LocalDateTime.now());

        Order saved = Order.builder().merchantUid("mu").price(500).version(0).build();
        // save only returns saved 객체
        when(txOp.transactional(any(Mono.class)))
                .thenReturn(Mono.just(saved));

        // findByMerchantUid 기본 stub이 Flux.empty() 를 반환하므로 filter/delete 로직 건너뜁니다
        StepVerifier.create(orderService.preparePayment(req))
                .assertNext(resp -> {
                    Assertions.assertEquals("mu", resp.getMerchantUid());
                    Assertions.assertEquals(500, resp.getRequestedAmount());
                })
                .verifyComplete();
    }

    @Test
    void 주문_제출_성공_및_이벤트발행() {
        OrderRequestDTO dto = Mockito.mock(OrderRequestDTO.class);
        CartItemRequestDTO item = Mockito.mock(CartItemRequestDTO.class);
        when(dto.getItems()).thenReturn(List.of(item));
        when(item.version()).thenReturn(0);
        when(item.menuName()).thenReturn("name");
        when(item.amount()).thenReturn(1);
        when(item.unitPrice()).thenReturn(1000);
        when(dto.getPayment()).thenReturn("card");
        when(dto.isPaymentSuccess()).thenReturn(true);
        when(dto.getUserUid()).thenReturn(3);
        when(dto.getSocialUid()).thenReturn(4);
        when(dto.getStoreUid()).thenReturn(5);
        when(dto.getMerchantUid()).thenReturn("mu");
        DeliveryAddressDTO addr = Mockito.mock(DeliveryAddressDTO.class);
        when(dto.getDeliveryAddress()).thenReturn(addr);
        when(dto.getReservationDate()).thenReturn(LocalDateTime.now());

        List<Order> savedList = List.of(Order.builder().uid(7).version(0).build());
        when(txOp.transactional(any(Mono.class)))
                .thenReturn(Mono.just(savedList));

        StepVerifier.create(orderService.submitOrder(dto))
                .assertNext(resp -> Assertions.assertTrue(resp.isSuccess()))
                .verifyComplete();
    }

    @Test
    void 주문_제출_실패_항목없음() {
        OrderRequestDTO dto = Mockito.mock(OrderRequestDTO.class);
        when(dto.getItems()).thenReturn(Collections.emptyList());

        StepVerifier.create(orderService.submitOrder(dto))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().equals("주문 항목이 없습니다."))
                .verify();
    }

    @Test
    void 상태_변경_성공() {
        Order orig = Order.builder().uid(8).merchantUid("m1").version(0).build();
        when(orderRepository.findByMerchantUid("m1"))
                .thenReturn(Flux.just(orig));
        when(orderRepository.saveAll(anyList()))
                .thenReturn(Flux.just(orig));

        StepVerifier.create(orderService.changeOrderStatus("m1", OrderStatus.ORDER_COOKING))
                .assertNext(r -> Assertions.assertTrue(r.isSuccess()))
                .verifyComplete();
    }

    @Test
    void 상태_변경_실패_잘못된상태() {
        // validateStatusForQueue 에 의해 ERROR 대신 success=false DTO 반환
        StepVerifier.create(orderService.changeOrderStatus("m1", OrderStatus.ORDER_CREATED))
                .assertNext(resp -> Assertions.assertFalse(resp.isSuccess()))
                .verifyComplete();
    }

    @Test
    void 지점별_주문_조회_기본상태() {
        DeliveryOrderResponseDTO dto = Mockito.mock(DeliveryOrderResponseDTO.class);
        when(deliveryOrderRepository.getStoreOrdersByStatusAndStoreUid(3, OrderStatus.PAYMENT_COMPLETED.name()))
                .thenReturn(Flux.just(dto));

        StepVerifier.create(orderService.getStoreOrders(3, null))
                .expectNext(dto)
                .verifyComplete();
    }
}


