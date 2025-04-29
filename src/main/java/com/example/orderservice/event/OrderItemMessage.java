package com.example.orderservice.event;

public record OrderItemMessage(
        String menuName,
        Integer amount,
        Double calorie,
        Integer unitPrice,
        int version
) {}
