package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record PaymentComplitedMessage (
        Integer uid,
        Integer userUid,
        Integer socialUid,
        LocalDateTime createdDate,
        LocalDateTime paidDate,
        OrderStatus status
){
}
