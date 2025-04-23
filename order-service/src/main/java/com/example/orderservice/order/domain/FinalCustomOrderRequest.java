package com.example.orderservice.order.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinalCustomOrderRequest {
    @NotNull
    private OrderRequestDTO orderRequestDTO;
    private CustomOrderRequestDTO customOrderRequestDTO;
}
