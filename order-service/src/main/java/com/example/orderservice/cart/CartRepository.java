package com.example.orderservice.cart;

import feign.Param;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Integer> {
    Flux<Cart> findByUserUid(Integer userUid);

    @Modifying
    @Query("UPDATE cart SET amount = :amount WHERE uid = :uid")
    Mono<Void> updateAmount(@Param("uid") Integer uid, @Param("amount") int amount);

}
