package com.example.orderservice.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "userClient", url = "http://localhost:9002")
public interface UserClient {

    @GetMapping("/auths/users/{userUid}")
    UserInfoResponseDTO getUserInfo(@PathVariable("userUid") Integer userUid);

    //주소 수정
    @PutMapping("/auths/{userUid}/address")
    UserInfoResponseDTO updateUserAddress(@PathVariable Integer userUid, @RequestBody UpdateAddressRequest request);

}
