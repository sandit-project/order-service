package com.example.orderservice.order.user;

import lombok.Getter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "userClient", url = "http://localhost:9002")
public interface UserClient {

    @GetMapping("/auths/users/{userUid}")
    UserInfoResponseDTO getUserInfo(@PathVariable("userUid") Integer userUid);
}
