package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

public record OrderCreatedMessage(
        Integer uid,
        OrderStatus status){
}
