package com.example.orderservice.cart;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("custom_cart")
public record CustomCart(
    @Id
    Integer uid,
    Integer bread,
    Integer material1,
    Integer material2,
    Integer material3,
    Integer vegetable1,
    Integer vegetable2,
    Integer vegetable3,
    Integer vegetable4,
    Integer vegetable5,
    Integer vegetable6,
    Integer vegetable7,
    Integer vegetable8,
    Integer sauce1,
    Integer sauce2,
    Integer sauce3,
    @Version
    int version
) {
}
