package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponseDTO {
    private Integer uid;
    private Integer userUid;
    private String socialUid;
    private List<CartItem> items;
    private Integer amount;
    private Integer price;
    private Double calorie;
    private String address;
    private String payment;
    private String merchantUid;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime reservationDate;
    private Integer version;
}
