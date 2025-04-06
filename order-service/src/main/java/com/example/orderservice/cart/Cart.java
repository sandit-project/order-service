package com.example.orderservice.cart;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("cart")
public record Cart(
    @Id
    Integer uid,
    Integer userUid,
    Integer socialUid,
    String menuName,
    int amount,
    Integer price,
    Double calorie,
    @CreatedDate
    LocalDateTime createdDate,
    @Version
    int version

){

}
