package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class OrderResponseDTO {
    private boolean success;
    private String message;
    private Integer orderUid;
}
