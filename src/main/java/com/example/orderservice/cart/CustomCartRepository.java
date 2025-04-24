package com.example.orderservice.cart;

import com.example.orderservice.order.domain.CustomOrder;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface CustomCartRepository extends ReactiveCrudRepository<CustomCart,Integer> {
    List<CustomOrder> uid(Integer uid);
    Mono<Void> deleteById(Integer uid);
}
