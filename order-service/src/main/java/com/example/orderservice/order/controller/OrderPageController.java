package com.example.orderservice.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderPageController {

    @GetMapping
    public String orderPage(Model model) {
        return "order";
    }

    @GetMapping("/custom")
    public String customPage(Model model) {
        return "custom-order";
    }
}
