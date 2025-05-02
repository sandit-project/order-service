package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderCreatedMessage (
        String merchantUid,
        OrderStatus status,
        LocalDateTime createdDate,
        LocalDateTime reservationDate
){
}
