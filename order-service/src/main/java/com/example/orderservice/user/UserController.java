package com.example.orderservice.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
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
    @GetMapping("/{uid}/address")
    public Mono<UserInfoResponseDTO> getUserAddresses(@PathVariable Integer uid) {
        return userService.getUserAddresses(uid);
    }

    //주소 수정
    @PutMapping("/{uid}/address")
    public Mono<UpdateAddressResponse> updateUserAddress(@PathVariable int uid, @RequestBody UpdateAddressRequest request) {
        log.info("update user address :: {}", request);
        return userService.updateAddress(uid,request);
    }

}
