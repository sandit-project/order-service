package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record CookingComplitedMessage (
        Integer uid,
        LocalDateTime createdDate,
        LocalDateTime cookedDate,
        OrderStatus status
){
}
