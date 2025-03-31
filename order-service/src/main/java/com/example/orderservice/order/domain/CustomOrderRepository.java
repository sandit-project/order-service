package com.example.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomOrderRepository extends ReactiveCrudRepository<CustomOrder, Integer> {
}
