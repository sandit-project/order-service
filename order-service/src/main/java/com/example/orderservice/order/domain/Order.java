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
        Integer userUid,
        Integer socialUid,
        //아이템들 직렬화
        String items,
        //String menuName,
        int amount,
        Integer price,
        Double calorie,
        //주소 추가?
        String address,
        String payment,
        //결제 식별자
        String merchantUid,
        OrderStatus status,

        @CreatedDate LocalDateTime createdDate,
        LocalDateTime reservationDate,
        @Version int version

){


}
