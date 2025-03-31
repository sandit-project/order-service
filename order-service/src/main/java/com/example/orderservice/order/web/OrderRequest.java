package com.example.orderservice.order.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest (
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
        String payment
) {
}
