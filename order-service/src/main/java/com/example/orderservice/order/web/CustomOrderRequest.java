package com.example.orderservice.order.web;

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
        int amount,
        @NotBlank(message = "payment must be defined")
        String payment
) {
}
