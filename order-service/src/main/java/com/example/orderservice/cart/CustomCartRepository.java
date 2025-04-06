package com.example.orderservice.cart;

import com.example.orderservice.order.domain.CustomOrder;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomCartRepository extends ReactiveCrudRepository<CustomOrder,Integer> {
}
