package com.example.orderservice.user;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserInfoResponseDTO getUserInfo(Integer userUid) {
        return UserInfoResponseDTO.builder()
                .userUid(1)
                .userId("test")
                .userName("홍길동")
                .email("test@gmail.com")
                .phone("010-1234-5678")
                .mainAddress("서울시 강동구 천호동")
                .subAddress1(null)
                .subAddress2(null)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Override
    public void updateUserAddress(Integer userUid, UpdateAddressRequest request) {
        // 아무것도 안 함 (실패)
        System.out.println("[WARN] User service unavailable. Address update skipped.");
    }
}
