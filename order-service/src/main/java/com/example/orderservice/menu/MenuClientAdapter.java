package com.example.orderservice.menu;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuClientAdapter {

    private final MenuClient menuClient;

    public Mono<Menu> getMenuByUid(Integer uid) {
        return Mono.fromCallable(() -> menuClient.getMenuByUid(uid))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(FeignException.NotFound.class, exception -> Mono.empty())
                .onErrorResume(Exception.class, ex -> {
                    log.error("General error: ", ex);
                    return Mono.empty();
                });
    }

    //메뉴 이름만 받아오기
    public Mono<String> getMenuByNameByUid(Integer uid) {
        return Mono.fromCallable(() -> menuClient.getMenuByUid(uid))
                .subscribeOn(Schedulers.boundedElastic())
                .map(Menu::menuName);
    }

    //메뉴 이름 검증 로직
    public Mono<Boolean> validateMenuName(String menuName) {
        return getAllMenus()
                .filter(menu -> menu.menuName().equalsIgnoreCase(menuName))
                .hasElements();
    }

    public Flux<Menu> getAllMenus() {
        return Mono.fromCallable(() -> menuClient.getMenus())
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(FeignException.NotFound.class, exception -> Mono.empty())
                .onErrorResume(Exception.class, ex -> {
                    log.error("General error: ", ex);
                    return Mono.empty();
                })
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Menu> enrollMenu(Menu menu) {
        return Mono.fromCallable(() -> menuClient.createMenu(
                Menu.builder()
                        .menuName(menu.menuName())
                        .price(menu.price())
                        .calorie(menu.calorie())
                        .bread(menu.bread())
                        .material1(menu.material1())
                        .material2(menu.material2())
                        .material3(menu.material3())
                        .cheese(menu.cheese())
                        .vegetable1(menu.vegetable1())
                        .vegetable2(menu.vegetable2())
                        .vegetable3(menu.vegetable3())
                        .vegetable4(menu.vegetable4())
                        .vegetable5(menu.vegetable5())
                        .vegetable6(menu.vegetable6())
                        .vegetable7(menu.vegetable7())
                        .vegetable8(menu.vegetable8())
                        .sauce1(menu.sauce1())
                        .sauce2(menu.sauce2())
                        .sauce3(menu.sauce3())
                        .image(menu.image())
                        .build()
        ))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(FeignException.NotFound.class, exception -> Mono.empty())
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }

    public Mono<Menu> updateMenu(Integer uid, Menu menu) {
        return Mono.fromCallable(() -> menuClient.updateMenu(uid, menu))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(FeignException.NotFound.class, exception -> Mono.empty())
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }

    public Mono<Void> deleteMenu(@PathVariable("uid") Integer uid) {
        return Mono.fromRunnable(() -> menuClient.deleteMenu(uid))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                )
                .onErrorResume(FeignException.NotFound.class, exception -> Mono.empty())
                .onErrorResume(Exception.class, exception -> Mono.empty()).then();

    }
}
