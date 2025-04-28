package com.example.orderservice.store;

import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DummyDataInitializer implements ApplicationRunner {

    private final StoreOrderRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        Flux<Order> dummyOrders = Flux.fromStream(
                IntStream.rangeClosed(1, 50).mapToObj(i ->
                        Order.builder()
                                .storeUid(3)
                                .userUid(100 + i)
                                .menuName("샌드위치 " + i)
                                .amount((i % 3) + 1)
                                .price((int) (7000L + (i * 100)))
                                .calorie((double) (400 + (i * 5)))
                                .status(i % 5 == 0 ? OrderStatus.PAYMENT_FAILED : OrderStatus.PAYMENT_COMPLETED)
                                .createdDate(LocalDateTime.now().minusDays(i))
                                .reservationDate(LocalDateTime.now().plusDays(i % 3))
                                .build()
                )
        );

        repository.saveAll(dummyOrders)
                .doOnComplete(() -> System.out.println("더미 주문 50건 저장 완료."))
                .subscribe();
    }
}
