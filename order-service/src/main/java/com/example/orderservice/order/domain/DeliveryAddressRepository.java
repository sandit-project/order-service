package com.example.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DeliveryAddressRepository extends ReactiveCrudRepository<DeliveryAddress, Integer> {
    Mono<DeliveryAddress> findByMerchantUid(String merchantUid);
}
