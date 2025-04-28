package com.example.orderservice.event;

public record AcceptOrderMessage(
        String merchantUid,
        String status
) {}
