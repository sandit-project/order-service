package com.example.orderservice.order.domain;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("custom_order")
public record CustomOrder(
        @Id
        Integer uid,
        Integer bread,
        Integer mainMaterial1,
        Integer mainMaterial2,
        Integer mainMaterial3,
        Integer cheeze,
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
        Integer sauce3

        ) {
}
