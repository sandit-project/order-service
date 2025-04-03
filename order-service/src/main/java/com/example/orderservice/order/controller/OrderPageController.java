package com.example.orderservice.order.controller;

import com.example.orderservice.menu.Menu;
import com.example.orderservice.menu.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import retrofit2.http.Path;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderPageController {
    private final MenuService menuService;

    @GetMapping({"", "/{menuUid}"})
    public Mono<String> orderPage(@PathVariable(value = "menuUid", required = false) Integer menuUid,
                                  Model model) {
        Mono<List<Menu>> menusMono = (menuUid != null)
                ? menuService.getMenuByUid(menuUid).map(List::of)
                : menuService.getAllMenus().collectList();

        return menusMono
                .doOnNext(menus -> model.addAttribute("menuList", menus))
                .thenReturn("order");
    }
}
