package com.example.orderservice.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderPageController {

    @GetMapping
    public String orderPage() {
        return "order";
    }
}
