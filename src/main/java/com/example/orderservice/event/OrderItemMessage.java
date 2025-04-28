package com.example.orderservice.event;

public record OrderItemMessage(
        String menuName,
        Integer amount,
        Integer unitPrice,
        int version
) {}
