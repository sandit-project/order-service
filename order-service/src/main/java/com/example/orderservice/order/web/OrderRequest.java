package com.example.orderservice.order.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest (
        Integer userUid,
        Integer socialUid,
        @NotBlank(message = "menu must be defined")
        String menuName,
        @NotNull(message = "amount must be defined")
        @Min(value = 1, message = "You must order at least 1 item.")
        int amount,
        @NotNull(message = "price must be defined")
        @Min(value = 1, message = "Price at least 1")
        int price,
        @NotBlank(message = "calorie must be defined")
        @Min(value = 1, message = "Calorie at least 1")
        Double calorie,
        @NotBlank(message = "payment must be defined")
        @Min(value = 1, message = "You must choose at least 1 payment method.")
        String payment,
        @NotBlank(message = "merchantUid must be defined")
        String merchantUid
) {
}
