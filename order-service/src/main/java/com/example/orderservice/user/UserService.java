package com.example.orderservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserClient userClient;

    // 유저 정보 가져오기
    public Mono<UserInfoResponseDTO> findByUserUid(Integer userUid) {
        return Mono.just(userClient.getUserInfo(userUid));
    }

    // 메인 주소 존재 여부 확인
    public Mono<Boolean> hasMainAddress(Integer userUid) {
        return Mono.just(userClient.getUserInfo(userUid))
                .map(user -> user.getMainAddress() != null && !user.getMainAddress().isBlank());
    }

    // 유저 주소 가져오기
    public Mono<UserInfoResponseDTO> getUserAddresses(Integer userUid) {
        return Mono.just(userClient.getUserInfo(userUid));
    }

    // 주소 업데이트
    public Mono<UserInfoResponseDTO> updateAddress(Integer userUid, UpdateAddressRequest request) {
        return Mono.just(userClient.updateUserAddress(userUid, request));
    }


}
