package com.example.orderservice.order.web;

import io.r2dbc.spi.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomOrderRequest(
        Integer userUid,
        Integer socialUid,
        //일단은 추가함 (아니면 빼기)
        @NotNull(message = "menu must be defined")
        Integer menuUid,
        @NotBlank(message = "menu must be defined")
        String menuName,
        @NotNull(message = "amount must be defined")
        @Min(value = 1, message = "You must order at least 1 item.")
        int amount,
        @NotBlank(message = "payment must be defined")
        @Min(value = 1, message = "You must choose at least 1 payment method.")
        String payment,


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
