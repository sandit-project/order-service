package com.example.orderservice.user;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("user")
public record User(
        @Id
        Integer uid,
        String userId,
        String password,
        String userName,
        String email,
        String phone,
        String phoneyn,
        String mainAddress,
        String subAddress1,
        String subAddress2,
        int point,
        UserStatus status,
        @CreatedDate LocalDateTime createdDate,
        @Version int version
) {
}
