package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderDispatchedMessage (
        String merchantUid,
        Integer userUid,
        Integer socialUid,
        Integer storeUid,
        LocalDateTime createdDate,
        OrderStatus status
){
}
