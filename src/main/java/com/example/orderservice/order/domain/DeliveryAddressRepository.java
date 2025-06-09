package com.example.orderservice.order.domain;

import com.example.orderservice.order.model.DeliveryAddress;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends ReactiveCrudRepository<DeliveryAddress, Long> {
    Mono<Boolean> existsByMerchantUid(String merchantUid);
    @Query("SELECT * FROM delivery_address WHERE merchant_uid = :merchantUid")
    Mono<DeliveryAddress> findByMerchantUid(@Param("merchantUid") String merchantUid);
}
