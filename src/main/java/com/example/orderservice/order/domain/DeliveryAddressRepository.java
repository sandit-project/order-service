package com.example.orderservice.order.domain;

import com.example.orderservice.order.model.DeliveryAddress;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends ReactiveCrudRepository<DeliveryAddress, Long> {
    Mono<Boolean> existsByMerchantUid(String merchantUid);
}
