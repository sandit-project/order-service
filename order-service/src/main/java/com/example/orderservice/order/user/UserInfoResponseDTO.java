package com.example.orderservice.order.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoResponseDTO {
    private Integer userUid;
    private String userId;
    private String password;
    private String userName;
    private String email;
    private boolean emailyn;
    private String phone;
    private boolean phoneyn;
    private String mainAddress;
    private String subAddress1;
    private String subAddress2;
    private int point;
    private UserStatus status;
    private LocalDateTime createdDate;
    private int version;
}
