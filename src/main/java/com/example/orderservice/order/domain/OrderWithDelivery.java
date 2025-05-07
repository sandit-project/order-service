package com.example.orderservice.order.domain;

import java.time.LocalDateTime;

public record OrderWithDelivery(
        Long uid,
        Long userUid,
        Long storeUid,
        String merchantUid,
        String menuName,
        int amount,
        Integer price,
        Double calorie,
        String payment,
        OrderStatus status,
        LocalDateTime createdDate,
        LocalDateTime reservationDate,
        String deliveryAddress   // delivery_address.address_destination
) {
}
