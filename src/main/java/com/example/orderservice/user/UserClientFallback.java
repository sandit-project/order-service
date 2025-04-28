package com.example.orderservice.user;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserInfoResponseDTO getUserInfo(Integer userUid) {
        // 사용자 정보 조회 실패 시 기본 응답
        return UserInfoResponseDTO.builder()
                .userUid(1)
                .userName("홍길동")
                .email("jr0503@naver.com")
                .mainAddress("서울시 강동구 풍성로 136-17")
                .subAddress1(null)
                .subAddress2(null)
                .phone("010-1234-1234")
                .build();
    }

    @Override
    public UpdateAddressResponse updateUserAddress(Integer userUid, UpdateAddressRequest request) {
        // 주소 수정 실패 시 기본 응답
        return UpdateAddressResponse.builder()
                .isSuccess(false)
                .build();
    }
}
