package com.example.orderservice.order.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UpdateOrderStatusRequest {

    private String merchantUid;
}
