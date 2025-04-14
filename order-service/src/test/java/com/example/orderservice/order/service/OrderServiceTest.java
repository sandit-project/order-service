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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        cartRepository = mock(CartRepository.class);
        orderService = new OrderService(orderRepository, cartRepository);
    }

    //예약 시간이 명시적으로 입력된 경우 그대로 저장
    @Test
    void reservationDate가_preparePayment에서_반영된다() {
        // given: 예약 시간이 있는 경우
        LocalDateTime reservationTime = LocalDateTime.of(2025, 4, 14, 18, 42);
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

        // when
        Mono<PreparePaymentResponseDTO> responseMono = orderService.preparePayment(request);

        // then: 예약 시간이 정상적으로 반영되어 있으면 그대로 저장되어야 함.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    verify(orderRepository).save(orderCaptor.capture());
                    Order savedOrder = orderCaptor.getValue();
                    // 예약 시간이 기본값과 차이가 없지 않은 경우 그대로 유지
                    assertNotNull(savedOrder.reservationDate(), "예약 시간이 입력되어 있으면 null이 아니어야 합니다.");
                    assertEquals(reservationTime, savedOrder.reservationDate());
                    assertEquals("사전 검증 및 저장 완료", response.getMessage());
                    assertEquals(savedOrder.merchantUid(), response.getMerchantUid());
                })
                .verifyComplete();
    }

    //예약 시간이 기본값(즉, 사용자가 수정하지 않아 페이지 로드시 자동 채워진 값)과 동일하면 null로 처리되어야 함.
    @Test
    void defaultReservationDate_shouldBeSavedAsNull_inPreparePayment() {
        // given: 클라이언트에서 페이지 로드시 설정된 기본값(현재 시간)으로 예약 시간이 세팅되어 있다고 가정
        // (테스트 시점의 현재 시간을 분 단위로 자른 값을 사용)
        LocalDateTime defaultTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        PreparePaymentRequestDTO request = PreparePaymentRequestDTO.builder()
                .merchantUid("test-merchant")
                .storeUid(2)
                .menuName("테스트 샌드위치")
                .totalPrice(5000)
                .userUid(1)
                .reservationDate(defaultTime)
                .build();

        // orderRepository.save() 호출 시 Order 객체 그대로 반환
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

        // when: preparePayment() 내부에서 예약 시간이 기본값과 가까우면 null로 처리하도록 되어 있음 (5분 미만 차이 기준)
        Mono<PreparePaymentResponseDTO> responseMono = orderService.preparePayment(request);

        // then: 캡처한 Order 객체의 reservationDate가 null이어야 함.
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    verify(orderRepository).save(orderCaptor.capture());
                    Order savedOrder = orderCaptor.getValue();
                    assertNull(savedOrder.reservationDate(),
                            "예약 시간이 기본값(현재 시간과 거의 동일)이면, null로 처리되어야 합니다.");
                })
                .verifyComplete();
    }

    //예약 시간이 명시적으로 입력된 경우 그대로 저장
    @Test
    void reservationDate가_submitOrder에서_반영된다() {
        // given: 사용자가 특정 예약 시간을 입력한 경우
        LocalDateTime reservationTime = LocalDateTime.of(2025, 4, 14, 18, 42);
        OrderRequestDTO orderRequestDTO = OrderRequestDTO.builder()
                .userUid(1)
                .storeUid(2)
                .items(List.of(new CartItem(1, "테스트 샌드위치", 1, 5000, 300.0)))
                .payment("카드")
                .merchantUid("test-merchant")
                .paymentSuccess(true)
                .reservationDate(reservationTime)
                .build();

        when(cartRepository.findById(anyInt()))
                .thenReturn(Mono.just(new Cart(1, 1, 2, "테스트 샌드위치", 1, 5000, 300.0, LocalDateTime.now(), 1)));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

        // when
        Flux<Order> result = orderService.submitOrder(orderRequestDTO);

        // then: 예약 시간이 사용자가 입력한 값 그대로 저장되어야 함.
        StepVerifier.create(result)
                .assertNext(order -> {
                    assertNotNull(order.reservationDate());
                    assertEquals(reservationTime, order.reservationDate());
                })
                .verifyComplete();
    }

    //주문 생성 시 예약 시간이 기본값과 동일하면 null로 저장되어야 함.
    @Test
    void defaultReservationDate_shouldBeSavedAsNull_inSubmitOrder() {
        // given: 기본값으로 채워진 예약 시간(페이지 로드시 설정된 값)과 동일한 값으로 요청
        LocalDateTime defaultTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        OrderRequestDTO orderRequestDTO = OrderRequestDTO.builder()
                .userUid(1)
                .storeUid(2)
                .items(List.of(new CartItem(1, "테스트 샌드위치", 1, 5000, 300.0)))
                .payment("카드")
                .merchantUid("test-merchant")
                .paymentSuccess(true)
                // 기본값으로 세팅된 시간이 그대로 들어온 경우
                .reservationDate(defaultTime)
                .build();

        when(cartRepository.findById(anyInt()))
                .thenReturn(Mono.just(new Cart(1, 1, 2, "테스트 샌드위치", 1, 5000, 300.0, LocalDateTime.now(), 1)));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0, Order.class)));

        // when
        Flux<Order> result = orderService.submitOrder(orderRequestDTO);

        // then: 서비스 로직에서 기본값과 거의 동일한 경우 null로 처리하도록 했으므로, Order의 reservationDate는 null이어야 함.
        StepVerifier.create(result)
                .assertNext(order -> {
                    assertNull(order.reservationDate(), "기본 예약 시간이 입력된 경우 null이어야 합니다.");
                })
                .verifyComplete();
    }
}