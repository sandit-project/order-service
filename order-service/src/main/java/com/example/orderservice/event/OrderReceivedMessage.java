package com.example.orderservice.event;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class OrderReceivedMessage{
    private final Integer uid;
    private final LocalDateTime createdDate;
}
