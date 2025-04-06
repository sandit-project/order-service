package com.example.orderservice.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Integer> {
    Flux<Cart> findByUserUid(Integer userUid);
}
