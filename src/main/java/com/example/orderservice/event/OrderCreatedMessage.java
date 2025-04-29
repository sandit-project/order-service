package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedMessage (
        String merchantUid,
        Integer userUid,
        Integer socialUid,
        Integer storeUid,
        DeliveryAddressMessage deliveryAddress,
        List<OrderItemMessage> items,
        OrderStatus status,
        LocalDateTime createdDate){
}
