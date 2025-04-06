package com.example.orderservice.cart;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CartRequestDTO {
    private Integer userUid;
    private String menuName;
    private int amount;
    private Integer price;
    private Double calorie;

}
