package com.example.orderservice.order.domain;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("orders")
public record Order (
        @Id
        Integer uid,
        Integer userUid,
        Integer socialUid,
        String menuName,
        int amount,
        Integer price,
        Double calorie,
        String payment,
        OrderStatus status,

        @CreatedDate LocalDateTime createdDate,
        LocalDateTime reservationDate,
        @Version int version

){


}
