package com.example.orderservice.order.domain;

import com.example.orderservice.order.model.DeliveryAddress;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DeliveryAddressRepository extends ReactiveCrudRepository<DeliveryAddress, Long> {
    Mono<DeliveryAddress> findByMerchantUid(String merchantUid);
}
