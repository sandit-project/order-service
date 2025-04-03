package com.example.orderservice.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuClientAdapter menuClientAdapter;

    public Mono<Menu> getMenuByUid(Integer uid) {
        return menuClientAdapter.getMenuByUid(uid)
                .doOnNext(getMenuByUid -> log.info("getMenuByUid {}", uid));
    }

    public Flux<Menu> getAllMenus() {
        return menuClientAdapter.getAllMenus()
                .doOnNext(getMenus -> log.info("getMenus {}", getMenus));
    }

    public Mono<Menu> enrollMenu(Menu menu) {
        return menuClientAdapter.enrollMenu(menu)
                .doOnNext(enrollMenu -> log.info("enroll menu: {}", enrollMenu));
    }

    public Mono<Menu> updateMenu(Integer uid, Menu menu) {
        return menuClientAdapter.updateMenu(uid, menu)
                .doOnNext(enrollMenu -> log.info("update menu: {}", updateMenu(uid, menu)));
    }

    public Mono<Void> deleteMenu(@PathVariable("uid") Integer uid) {
        return menuClientAdapter.deleteMenu(uid);

    }
}
