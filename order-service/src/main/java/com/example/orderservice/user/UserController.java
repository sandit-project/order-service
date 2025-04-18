package com.example.orderservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserClient userClient;

    //회원 정보 확인 (통합 시 수정)
    @GetMapping("/{uid}")
    public Mono<UserInfoResponseDTO> getUserInfo(@PathVariable Integer uid) {
        return userService.findByUserUid(uid);
    }

    //주소 확인 여부
    @GetMapping("/{uid}/check-address")
    public Mono<CheckAddressResponse> checkUserAddress(@PathVariable Integer uid) {
        return userService.hasMainAddress(uid)
                .map(hasAddress -> CheckAddressResponse.builder()
                        .hasAddress(hasAddress)
                        .build());
    }

    //주소 가져오기
    @GetMapping("/{uid}/addresses")
    public Mono<UserInfoResponseDTO> getUserAddresses(@PathVariable Integer uid) {
        return userService.getUserAddresses(uid);
    }

    //주소 입력
    @PostMapping("/{uid}/addresses")
    public Mono<UserInfoResponseDTO> updateUserAddress(@PathVariable Integer uid, @RequestBody UpdateAddressRequest request) {
        return Mono.fromCallable(() ->
                userClient.updateUserAddress(uid, request)
        );
    }

}
