package com.example.orderservice.user;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("user_address")
public record Address (
    Integer uid,
    Integer userUid,
    Integer socialUid,
    String mainAddress,
    String subAddress1,
    String subAddress2,
    double mainLat,
    double mainLan,
    double sub1Lat,
    double sub1Lan,
    double sub2Lat,
    double sub2Lan
){
}
