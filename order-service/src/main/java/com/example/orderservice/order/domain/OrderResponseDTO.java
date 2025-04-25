package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDTO {
    private boolean success;
    private String message;
    private Integer orderUid;
}
