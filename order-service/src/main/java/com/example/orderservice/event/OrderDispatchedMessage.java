package com.example.orderservice.event;

public record OrderDispatchedMessage (
        Integer uid,
        String status
){
}
