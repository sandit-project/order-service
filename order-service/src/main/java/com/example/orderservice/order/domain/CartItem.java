package com.example.orderservice.order.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
//메뉴 1개의 정보
public record CartItem (
        @NotBlank(message = "menu must be defined")
        String menuName,
        @NotNull(message = "amount must be defined")
        @Min(value = 1, message = "You must order at least 1 item.")
        int amount,
        @NotNull(message = "price must be defined")
        @Min(value = 1, message = "Price at least 1")
        int price,
        @NotNull(message = "calorie must be defined")
        @Min(value = 1, message = "Calorie at least 1")
        Double calorie
){

}
