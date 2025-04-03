package com.example.orderservice.order.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomOrderRequest{
        @NotNull(message = "orderRequest must be provided")
        private OrderRequestDTO orderRequestDTO;

        @NotNull(message = "bread must be defined")
        private Integer bread;
        @NotNull(message = "material 1 must be defined")
        private Integer material1;
        private Integer material2;
        private Integer material3;
        private Integer cheese;
        @NotNull(message = "vegetable 1 must be defined")
        private Integer vegetable1;
        private Integer vegetable2;
        private Integer vegetable3;
        private Integer vegetable4;
        private Integer vegetable5;
        private Integer vegetable6;
        private Integer vegetable7;
        private Integer vegetable8;
        @NotNull(message = "sauce 1 must be defined")
        private Integer sauce1;
        private Integer sauce2;
        private Integer sauce3;
}
