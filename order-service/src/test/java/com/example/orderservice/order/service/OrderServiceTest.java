package com.example.orderservice.order.service;

import com.example.orderservice.cart.Cart;
import com.example.orderservice.cart.CartRepository;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.domain.OrderStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        cartRepository = mock(CartRepository.class);
        orderService = new OrderService(orderRepository, cartRepository);
    }

    @Test
    void 정상_주문저장_성공() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userUid(1)
                .storeUid(2)
                .items(List.of(new com.example.orderservice.order.domain.CartItem(1, "에그마요 샌드위치", 1, 5000, 300.0)))
                .payment("카드")
                .merchantUid("test-merchant")
                .paymentSuccess(true)
                .reservationDate(LocalDateTime.now())
                .build();

        when(cartRepository.findById((Integer) any()))
                .thenReturn(Mono.just(new Cart(1, 1, 2, "햄치즈 샌드위치", 1,10000, 300.0, LocalDateTime.now(), 1)));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.just(new Order(1, 1, 2, 3, "merchant-1234", "햄치즈 샌드위치", 1, 10000, 300.0, "카드", ORDER_CREATED, LocalDateTime.now(), LocalDateTime.now(),1)));

        Flux<Order> result = orderService.submitOrder(request);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void 결제성공시_상태업데이트() {
        when(orderRepository.findByMerchantUid(any()))
                .thenReturn(Mono.just(new Order(1, 1, 2, 3, "merchant-1234", "햄치즈 샌드위치", 1, 10000, 300.0, "카드", PAYMENT_COMPLETED, LocalDateTime.now(), LocalDateTime.now(),1)));

        when(orderRepository.updateOrderStatus(anyInt(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateOrderStatusToSuccess("merchant-uid"))
                .verifyComplete();
    }

    @Test
    void 결제실패시_상태업데이트() {
        when(orderRepository.findByMerchantUid(any()))
                .thenReturn(Mono.just(new Order(1, 1, 2, 3, "merchant-1234", "햄치즈 샌드위치", 1, 10000, 300.0, "카드", PAYMENT_FAILED, LocalDateTime.now(), LocalDateTime.now(),1)));

        when(orderRepository.updateOrderStatus(anyInt(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateOrderStatusToFailed("merchant-uid"))
                .verifyComplete();
    }
}