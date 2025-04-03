package com.example.orderservice.order.service;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final static Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final MenuClientAdapter menuClientAdapter;
    private final CustomOrderRepository customOrderRepository;

    @PostMapping
    public Mono<CustomOrder> submitCustomOrder(@RequestBody CustomOrderRequest customOrderRequest,
                                               Integer uid, List<CartItem> items) {
        return menuClientAdapter.getMenuByUid(uid)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Menu not found with id: ")))
                .flatMap(menu -> {
                    CustomOrder customOrder = CustomOrder.builder()
                            .uid(uid)
                            .bread(customOrderRequest.getBread())
                            .material1(customOrderRequest.getMaterial1())
                            .material2(customOrderRequest.getMaterial2())
                            .material3(customOrderRequest.getMaterial3())
                            .cheese(customOrderRequest.getCheese())
                            .vegetable1(customOrderRequest.getVegetable1())
                            .vegetable2(customOrderRequest.getVegetable2())
                            .vegetable3(customOrderRequest.getVegetable2())
                            .vegetable4(customOrderRequest.getVegetable4())
                            .vegetable5(customOrderRequest.getVegetable5())
                            .vegetable6(customOrderRequest.getVegetable6())
                            .vegetable7(customOrderRequest.getVegetable7())
                            .vegetable8(customOrderRequest.getVegetable8())
                            .sauce1(customOrderRequest.getSauce1())
                            .sauce2(customOrderRequest.getSauce2())
                            .sauce3(customOrderRequest.getSauce3())
                            .build();
                    return customOrderRepository.save(customOrder);
                });
    }
}
