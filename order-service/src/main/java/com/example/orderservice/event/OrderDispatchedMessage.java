package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderDispatchedMessage (
        Integer uid,
        Integer userUid,
        Integer socialUid,
        LocalDateTime createdDate,
        OrderStatus status
){
}
