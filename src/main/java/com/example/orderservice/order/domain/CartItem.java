package com.example.orderservice.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Version;

//메뉴 1개의 정보 (프론트랑 통신하는 DTO)
public record CartItem (
        @JsonProperty("uid")
        Integer uid,

        @JsonProperty("menuName")
        @NotBlank(message = "menu must be defined")
        String menuName,

        @JsonProperty("amount")
        @NotNull(message = "amount must be defined")
        @Min(value = 1, message = "You must order at least 1 item.")
        int amount,

        @JsonProperty("unitPrice")
        @NotNull(message = "price must be defined")
        @Min(value = 1, message = "Price at least 1")
        int unitPrice,

        @JsonProperty("calorie")
        @NotNull(message = "calorie must be defined")
        @Min(value = 1, message = "Calorie at least 1")
        Double calorie,

        @JsonProperty("version")
        @Version
        Integer version
){
        @JsonCreator
        public CartItem{}
}
