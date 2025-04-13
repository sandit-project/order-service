package com.example.orderservice.order.domain;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("orders")
public record Order (
        @Id
        Integer uid,
        @Column("user_uid")
        Integer userUid,
        @Column("social_uid")
        Integer socialUid,
        @Column("store_uid")
        Integer storeUid,
        @Column("merchant_uid")
        String merchantUid,
        @Column("menu_name")
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
