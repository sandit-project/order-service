package com.example.orderservice.order.controller;

import com.example.orderservice.menu.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderPageController {
    private final MenuService menuService;

    @GetMapping("/{menuUid}")
    public Mono<String> orderPage(
            @PathVariable("menuUid") Integer menuUid, Model model) {

        return menuService.getMenuByUid(menuUid)
                .doOnNext(menu -> model.addAttribute("menu", menu))
                .thenReturn("order");
    }
}
