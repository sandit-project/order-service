package com.example.orderservice.order.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomOrderRequest(
        @NotNull(message = "orderRequest must be provided")
        OrderRequest orderRequest,

        @NotNull(message = "bread must be defined")
        Integer bread,
        @NotNull(message = "material 1 must be defined")
        Integer material1,
        Integer material2,
        Integer material3,
        Integer cheese,
        @NotNull(message = "vegetable 1 must be defined")
        Integer vegetable1,
        Integer vegetable2,
        Integer vegetable3,
        Integer vegetable4,
        Integer vegetable5,
        Integer vegetable6,
        Integer vegetable7,
        Integer vegetable8,
        @NotNull(message = "sauce 1 must be defined")
        Integer sauce1,
        Integer sauce2,
        Integer sauce3
) {
}
