package com.example.orderservice.order.service;

import com.example.orderservice.cart.Cart;
import com.example.orderservice.cart.CartRepository;
import com.example.orderservice.order.domain.CartItemRequestDTO;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
    class OrderServiceTest {

        private OrderRepository orderRepository;
        private CartRepository cartRepository;
        private OrderService orderService;
        private TransactionalOperator txOp;


        @Captor
        private ArgumentCaptor<Order> orderCaptor;

        // 고정된 현재 시간으로 테스트할 수 있도록 OrderService의 익명 서브클래스를 생성
        @BeforeEach
        void setUp() {
            orderRepository = mock(OrderRepository.class);
            cartRepository = mock(CartRepository.class);
            txOp            = mock(TransactionalOperator.class);

            // 트랜잭션 콜백을 실제로 실행하도록 전역 모킹
            when(txOp.execute(any(TransactionCallback.class)))
                    .thenAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        TransactionCallback<Order> callback =
                                invocation.getArgument(0, TransactionCallback.class);
                        // doInTransaction 에 null을 넘겨도 내부 로직 실행됨
                        return Flux.from(callback.doInTransaction(null));
                    });

            orderService = new OrderService(orderRepository, cartRepository, txOp) {
                @Override
                protected LocalDateTime getNow() {
                    // 테스트 전용 고정 현재 시각
                    return LocalDateTime.of(2025, 4, 16, 12, 14);
                }
            };
        }

        @Test
        void 준비_결제_예약시간_내부_5분이면_null로_저장된다() {
            // given
            LocalDateTime now = orderService.getNow();
            PreparePaymentRequestDTO req = PreparePaymentRequestDTO.builder()
                    .merchantUid("m1").storeUid(2).menuName("샌드위치")
                    .totalPrice(1000).userUid(10).reservationDate(now)
                    .build();

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(i -> Mono.just(i.getArgument(0, Order.class)));

            // when
            Mono<PreparePaymentResponseDTO> mono = orderService.preparePayment(req);

            // then
            StepVerifier.create(mono)
                    .assertNext(resp -> {
                        // save()에 전달된 Order 객체를 캡처해서 reservationDate 검증
                        verify(orderRepository).save(orderCaptor.capture());
                        Order saved = orderCaptor.getValue();
                        assertNull(saved.reservationDate(),
                                "5분 이내 예약시간은 null로 저장되어야 함");
                        assertEquals("사전 검증 및 저장 완료", resp.getMessage());
                    })
                    .verifyComplete();
        }

        @Test
        void 준비_결제_5분_이후_예약시간_존재_검증() {
            LocalDateTime later = orderService.getNow().plusMinutes(10);
            PreparePaymentRequestDTO req = PreparePaymentRequestDTO.builder()
                    .merchantUid("m2").storeUid(3).menuName("샌드위치")
                    .totalPrice(2000).userUid(20).reservationDate(later)
                    .build();

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(i -> Mono.just(i.getArgument(0, Order.class)));

            StepVerifier.create(orderService.preparePayment(req))
                    .assertNext(resp -> {
                        verify(orderRepository).save(orderCaptor.capture());
                        Order saved = orderCaptor.getValue();
                        assertNotNull(saved.reservationDate(),
                                "5분 이후 예약시간은 유지되어야 함");
                    })
                    .verifyComplete();
        }

        @Test
        void submitOrder_예약시간_5분_이내이면_null() {
            LocalDateTime reserve = orderService.getNow().plusMinutes(1);
            OrderRequestDTO dto = OrderRequestDTO.builder()
                    .userUid(1).storeUid(2)
                    .items(List.of(new CartItemRequestDTO(1, "A", 1, 1000, 100.0)))
                    .payment("CARD").merchantUid("m3")
                    .paymentSuccess(true).reservationDate(reserve).build();

            when(cartRepository.findById(anyInt()))
                    .thenReturn(Mono.just(new Cart(1,1,2,"A",1,1000,100.0,
                            orderService.getNow(), 1)));
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(i -> Mono.just(i.getArgument(0, Order.class)));

            StepVerifier.create(orderService.submitOrder(dto))
                    .assertNext(order -> {
                        assertNull(order.reservationDate());
                    })
                    .verifyComplete();
        }

        @Test
        void submitOrder_예약시간_5분_초과하면_유지() {
            LocalDateTime reserve = orderService.getNow().minusMinutes(10);
            OrderRequestDTO dto = OrderRequestDTO.builder()
                    .userUid(1).storeUid(2)
                    .items(List.of(new CartItemRequestDTO(1, "B", 1, 2000, 200.0)))
                    .payment("CARD").merchantUid("m4")
                    .paymentSuccess(true).reservationDate(reserve).build();

            when(cartRepository.findById(anyInt()))
                    .thenReturn(Mono.just(new Cart(1,1,2,"B",1,2000,200.0,
                            orderService.getNow(), 1)));
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(i -> Mono.just(i.getArgument(0, Order.class)));

            StepVerifier.create(orderService.submitOrder(dto))
                    .assertNext(order -> {
                        assertNotNull(order.reservationDate());
                    })
                    .verifyComplete();
        }
    }
