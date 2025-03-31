package com.example.orderservice.event;

public record PaymentFailedMessage (
        Integer uid,
        String status
) {
}
