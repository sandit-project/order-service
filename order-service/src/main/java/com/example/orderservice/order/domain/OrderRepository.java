package com.example.orderservice.order.domain;

import feign.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    @Query("SELECT * FROM `orders`")
    Flux<Order> findAllOrders();

    @Query("SELECT * FROM `orders` WHERE `user_uid` = :userUid")
    Flux<Order> findByUserUid(@Param("userUid") Integer userUid);

    @Query("SELECT * FROM `orders` WHERE `merchant_uid` = :merchantUid")
    Mono<Order> findByMerchantUid(String merchantUid);
}
