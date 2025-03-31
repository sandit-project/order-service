package com.example.orderservice.order.domain;

import io.r2dbc.spi.Parameter;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("order")
public record Order (
        @Id
        Integer uid,
        Integer userUid,
        Integer socialUid,
        //일단은 추가함 (아니면 빼기)
        Integer menuUid,
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
