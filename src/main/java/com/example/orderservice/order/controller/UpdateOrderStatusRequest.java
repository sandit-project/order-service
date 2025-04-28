package com.example.orderservice.order.controller;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateOrderStatusRequest {

    private String merchantUid;
}
