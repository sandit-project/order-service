package com.example.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Flux<Order> findByUserUid(Integer userUid);
    Mono<Order> findByMerchantUid(String merchantUid);
}
