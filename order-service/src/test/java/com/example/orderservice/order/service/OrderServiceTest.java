package com.example.orderservice.order.service;

import com.example.orderservice.cart.Cart;
import com.example.orderservice.cart.CartRepository;
import com.example.orderservice.order.domain.CartItem;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

    class OrderServiceTest {

        private OrderRepository orderRepository;
        private CartRepository cartRepository;
        private OrderService orderService;

        // 고정된 현재 시간으로 테스트할 수 있도록 OrderService의 익명 서브클래스를 생성
        @BeforeEach
        void setUp() {
            orderRepository = mock(OrderRepository.class);
            cartRepository = mock(CartRepository.class);
            orderService = new OrderService(orderRepository, cartRepository) {
                @Override
                protected LocalDateTime getNow() {
                    // 테스트 전용 고정 현재 시각
                    return LocalDateTime.of(2025, 4, 16, 12, 14);
                }
            };
        }

        // (1) preparePayment: 사용자가 예약 시간을 지정한 경우 그대로 저장되어야 함.
        @Test
        void preparePayment_shouldSaveProvidedReservationDate() {
            LocalDateTime reservationTime = LocalDateTime.of(2025, 4, 16, 12, 20);
            PreparePaymentRequestDTO request = PreparePaymentRequestDTO.builder()
                    .merchantUid("test-merchant")
                    .storeUid(2)
                    .menuName("테스트 샌드위치")
                    .totalPrice(5000)
                    .userUid(1)
                    .reservationDate(reservationTime)
                    .build();

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

            Mono<PreparePaymentResponseDTO> responseMono = orderService.preparePayment(request);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            StepVerifier.create(responseMono)
                    .assertNext(response -> {
                        verify(orderRepository).save(orderCaptor.capture());
                        Order savedOrder = orderCaptor.getValue();
                        assertNotNull(savedOrder.reservationDate(), "예약 시간이 지정되었으면 null이 아니어야 합니다.");
                        assertEquals(reservationTime.truncatedTo(ChronoUnit.MINUTES),
                                savedOrder.reservationDate().truncatedTo(ChronoUnit.MINUTES));
                    })
                    .verifyComplete();
        }

        // (2) preparePayment: 예약 시간이 기본값(현재 시각과 5분 미만 차)인 경우 null 처리되어야 함.
        @Test
        void preparePayment_shouldSaveReservationDateAsNull_ifDefaultValue() {
            // 테스트용 고정된 현재 시각은 2025-04-16T12:14 (setUp()에서 지정)
            LocalDateTime defaultTime = LocalDateTime.of(2025, 4, 16, 12, 14);
            PreparePaymentRequestDTO request = PreparePaymentRequestDTO.builder()
                    .merchantUid("test-merchant")
                    .storeUid(2)
                    .menuName("테스트 샌드위치")
                    .totalPrice(5000)
                    .userUid(1)
                    .reservationDate(defaultTime)
                    .build();

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

            Mono<PreparePaymentResponseDTO> responseMono = orderService.preparePayment(request);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            StepVerifier.create(responseMono)
                    .assertNext(response -> {
                        verify(orderRepository).save(orderCaptor.capture());
                        Order savedOrder = orderCaptor.getValue();
                        assertNull(savedOrder.reservationDate(), "기본 예약 시간이면 null이어야 합니다.");
                    })
                    .verifyComplete();
        }

        // (3) submitOrder: 사용자가 예약 시간을 지정한 경우 그대로 저장되어야 함.
        @Test
        void submitOrder_shouldSaveProvidedReservationDate() {
            LocalDateTime reservationTime = LocalDateTime.of(2025, 4, 16, 12, 30);
            OrderRequestDTO orderRequestDTO = OrderRequestDTO.builder()
                    .userUid(1)
                    .storeUid(2)
                    .items(List.of(new CartItem(1, "테스트 샌드위치", 1, 5000, 300.0)))
                    .payment("CARD")
                    .merchantUid("test-merchant")
                    .paymentSuccess(true)
                    .reservationDate(reservationTime)
                    .build();

            when(cartRepository.findById(anyInt()))
                    .thenReturn(Mono.just(new Cart(1, 1, 2, "테스트 샌드위치", 1, 5000, 300.0, LocalDateTime.of(2025,4,16,12,10), 1)));
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

            Flux<Order> result = orderService.submitOrder(orderRequestDTO);

            StepVerifier.create(result)
                    .assertNext(order -> {
                        assertNotNull(order.reservationDate());
                        assertEquals(reservationTime.truncatedTo(ChronoUnit.MINUTES),
                                order.reservationDate().truncatedTo(ChronoUnit.MINUTES));
                    })
                    .verifyComplete();
        }

        // (4) submitOrder: 예약 시간이 기본값(현재 시각과 거의 동일, 즉 2025-04-16T12:14)인 경우 null 처리되어야 함.
        @Test
        void submitOrder_shouldSaveReservationDateAsNull_ifDefaultValue() {
            LocalDateTime defaultTime = LocalDateTime.of(2025, 4, 16, 12, 14); // 고정된 현재 시각과 동일
            OrderRequestDTO orderRequestDTO = OrderRequestDTO.builder()
                    .userUid(1)
                    .storeUid(2)
                    .items(List.of(new CartItem(1, "테스트 샌드위치", 1, 5000, 300.0)))
                    .payment("CARD")
                    .merchantUid("test-merchant")
                    .paymentSuccess(true)
                    .reservationDate(defaultTime)
                    .build();

            when(cartRepository.findById(anyInt()))
                    .thenReturn(Mono.just(new Cart(1, 1, 2, "테스트 샌드위치", 1, 5000, 300.0, LocalDateTime.of(2025,4,16,12,10), 1)));
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

            Flux<Order> result = orderService.submitOrder(orderRequestDTO);

            StepVerifier.create(result)
                    .assertNext(order -> {
                        assertNull(order.reservationDate(), "기본 예약 시간이 입력된 경우 null이어야 합니다.");
                    })
                    .verifyComplete();

            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertNull(savedOrder.reservationDate(), "저장된 Order의 예약 시간이 null이어야 합니다.");
        }
    }
