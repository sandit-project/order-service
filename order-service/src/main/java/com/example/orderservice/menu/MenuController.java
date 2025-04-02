package com.example.orderservice.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public Flux<Menu> getMenus() {
        return menuService.getMenus();
    }

    @GetMapping("/{uid}")
    public Mono<Menu> getMenuByUid(@PathVariable Integer uid) {
        return menuService.getMenuByUid(uid);
    }

    @PostMapping
    public Mono<Menu> enrollMenu(@RequestBody Menu menu) {
        return menuService.enrollMenu(menu);
    }

    @PutMapping("/{uid}")
    public Mono<Menu> updateMenu(@PathVariable ("uid") Integer uid, @RequestBody Menu menu) {
        return menuService.updateMenu(uid, menu);
    }

    @DeleteMapping("/{uid}")
    public Mono<Void> deleteMenu(@PathVariable ("uid") Integer uid) {
        return menuService.deleteMenu(uid);
    }
}
