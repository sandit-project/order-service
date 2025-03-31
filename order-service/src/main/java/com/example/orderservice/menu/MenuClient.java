package com.example.orderservice.menu;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient (name = "menuClient", url = "${menu-service.url}")
public interface MenuClient {

    @GetMapping("/menu/{uid}")
    Menu getMenuByUid(@PathVariable ("uid") Integer uid);

    @GetMapping("/menu")
    List<Menu> getMenus();

    @PostMapping("/menu")
    Menu createMenu(@RequestBody Menu menu);

    @PutMapping("/menu/{uid}")
    Menu updateMenu(@PathVariable ("uid") Integer uid, @RequestBody Menu menu);

    @DeleteMapping("/menu/{uid}")
    void deleteMenu(@PathVariable ("uid") Integer uid);

}
