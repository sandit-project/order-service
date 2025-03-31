package com.example.orderservice.event;

public record OrderCreatedMessage(
        Integer uid,
        com.example.orderservice.order.domain.OrderStatus status){
}
