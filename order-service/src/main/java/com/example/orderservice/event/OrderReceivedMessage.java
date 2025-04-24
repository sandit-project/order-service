package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

public record OrderReceivedMessage (
        Integer uid,
        Integer userUid,
        Integer socialUid,
        LocalDateTime createdDate,
        OrderStatus status
){
}
