package com.example.orderservice.menu;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
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

    public Flux<Menu> getMenus() {
        return menuClientAdapter.getAllMenus()
                .doOnNext(getMenus -> log.info("getMenus {}", getMenus));
    }

    public Mono<Menu> enrollMenu(Menu menu) {
        return menuClientAdapter.enrollMenu(menu)
                .doOnNext(enrollMenu -> log.info("enroll menu: {}", enrollMenu));
    }
}
