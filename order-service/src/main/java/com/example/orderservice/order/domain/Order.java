package com.example.orderservice.order.domain;

import io.r2dbc.spi.Parameter;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("order")
public record Order (
        @Id
        @Column("uid")
        Integer uid,
        @Column("user_uid")
        Integer userUid,
        @Column("social_uid")
        Integer socialUid,
        @Column("menu_name")
        String menuName,
        int amount,
        Integer price,
        String payment,
        String status,
        @Column("created_date")
        LocalDateTime createdDate,
        @Column("reservation_date")
        LocalDateTime reservationDate,
        int version

){


}
