package com.example.orderservice.order.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class FinalCustomOrderRequest {
    @NotNull
    private OrderRequestDTO orderRequestDTO;
    private List<CustomOrderRequestDTO> customOrderRequestDTO;
}
