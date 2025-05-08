package com.example.orderservice.order.service;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.DeliveryAddress;
import com.example.orderservice.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private TransactionalOperator txOp;

    @InjectMocks
    private OrderService orderService;

    private CartItemRequestDTO createItem(String name, int amount, int price, double calorie, int version) {
        return new CartItemRequestDTO(
                1,
                name,
                amount,
                price,
                calorie,
                version
        );
    }

    @Test
    @DisplayName("주문항목이_없으면_예외가_발생한다")
    void 주문항목이_없으면_예외가_발생한다() {
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .storeUid(1)
                .items(Collections.emptyList())
                .payment("card")
                .merchantUid("muid123")
                .version(0)
                .build();

        StepVerifier.create(orderService.submitOrder(dto))
                .expectErrorMessage("주문 항목이 없습니다.")
                .verify();
    }

    @Test
    @DisplayName("대표주문이면_저장없이_완료된다")
    void 대표주문이면_저장없이_완료된다() {
        CartItemRequestDTO item = createItem("외 2건", 1, 10000, 100.0, 1);
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .storeUid(1)
                .items(List.of(item))
                .payment("card")
                .merchantUid("muid-skip")
                .version(1)
                .build();

        StepVerifier.create(orderService.submitOrder(dto))
                .verifyComplete();

        verify(orderRepository, never()).saveAll(anyList());
        verify(deliveryAddressRepository, never()).existsByMerchantUid(anyString());
    }

    @Test
    @DisplayName("이미_저장된_merchantUid이면_주소저장_스킵된다")
    void 이미_저장된_merchantUid이면_주소저장_스킵된다() {
        CartItemRequestDTO item = createItem("menu1", 2, 2000, 50.0, 0);
        DeliveryAddressDTO addrDto = DeliveryAddressDTO.builder()
                .addressStart("서울시")
                .addressStartLat(37.1)
                .addressStartLan(127.1)
                .addressDestination("부산시")
                .addressDestinationLat(35.1)
                .addressDestinationLan(129.1)
                .build();

        OrderRequestDTO dto = OrderRequestDTO.builder()
                .storeUid(1)
                .items(List.of(item))
                .deliveryAddress(addrDto)
                .payment("card")
                .merchantUid("dup-muid")
                .version(0)
                .build();

        Order savedOrder = Order.builder().uid(1).merchantUid("dup-muid").build();
        when(orderRepository.saveAll(anyList()))
                .thenReturn(Flux.just(savedOrder));
        when(txOp.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(deliveryAddressRepository.existsByMerchantUid("dup-muid"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderService.submitOrder(dto))
                .expectNextMatches(resp -> resp.isSuccess())
                .verifyComplete();

        verify(deliveryAddressRepository, never()).save(any(DeliveryAddress.class));
    }

    @Test
    @DisplayName("주문저장시_MQ가_발행된다")
    void 주문저장시_MQ가_발행된다() {
        CartItemRequestDTO item = createItem("egg", 3, 4000, 40.0, 0);
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .storeUid(1)
                .items(List.of(item))
                .payment("card")
                .merchantUid("mq-muid")
                .version(0)
                .build();

        when(orderRepository.saveAll(anyList()))
                .thenReturn(Flux.just(Order.builder().uid(10).merchantUid("mq-muid").build()));
        when(txOp.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(orderService.submitOrder(dto))
                .expectNextMatches(resp -> resp.isSuccess() && resp.getOrderUid() == 10)
                .verifyComplete();

        ArgumentCaptor<OrderCreatedMessage> captor = ArgumentCaptor.forClass(OrderCreatedMessage.class);
        verify(streamBridge).send(eq("orderCreated-out-0"), captor.capture());
        OrderCreatedMessage msg = captor.getValue();
        assertThat(msg.merchantUid()).isEqualTo("mq-muid");
    }
}
