package com.example.orderservice.menu;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "menuClient", url = "${menu-service.url}")
public interface MenuClient {

    //스토어 주소에 맞게 바꾸면 됨
    @GetMapping("/stores")
    List<StoreResponseDTO> getStores();

    @GetMapping("/menus/cart/user/{userUid}")
    CartResponseDTO getCartItemsByUserUid(@PathVariable("userUid") Long userUid);
}
