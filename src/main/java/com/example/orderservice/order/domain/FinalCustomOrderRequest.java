package com.example.orderservice.order.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class FinalCustomOrderRequest {
    @NotNull
    private OrderRequestDTO orderRequestDTO;
    private CustomOrderRequestDTO customOrderRequestDTO;
}
