package com.example.orderservice.menu;

import com.example.orderservice.dummy.MenuClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient (name = "menuClient", url = "${menu-service.url}", fallback = MenuClientFallback.class)
public interface MenuClient {

    @GetMapping("/menu/{uid}")
    Menu getMenuByUid(@PathVariable ("uid") Integer uid);

    //모든 메뉴 조회
    @GetMapping("/menu")
    List<Menu> getMenus();

    //카테고리별 메뉴 조회 (추후에 추가 예정)

    @PostMapping("/menu")
    Menu createMenu(@RequestBody Menu menu);

    @PutMapping("/menu/{uid}")
    Menu updateMenu(@PathVariable ("uid") Integer uid, @RequestBody Menu menu);

    @DeleteMapping("/menu/{uid}")
    void deleteMenu(@PathVariable ("uid") Integer uid);

}
