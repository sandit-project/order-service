package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderCreatedMessage (
        String merchantUid,
        Integer userUid,
        Integer socialUid,
        Integer deliveryManUid,
        //자체인지 소셜인지 구분
        String deliveryManType,
        Integer storeUid,
        DeliveryAddressMessage deliveryAddress,
        List<OrderItemMessage> items,
        OrderStatus status,
        LocalDateTime createdDate,
        boolean republished
){
}
