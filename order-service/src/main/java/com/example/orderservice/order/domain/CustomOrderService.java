package com.example.orderservice.order.domain;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.web.CustomOrderRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final static Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final StreamBridge streamBridge;
    private final MenuClientAdapter menuClientAdapter;
    private final CustomOrderRepository customOrderRepository;

    @PostMapping
    public Mono<CustomOrder> submitCustomOrder(@RequestBody CustomOrderRequest customOrderRequest,
                                         Integer uid, Integer menuUid) {
        return menuClientAdapter.getMenuByUid(menuUid)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Menu not found with id: " +menuUid)))
                .flatMap(menu -> {
                    CustomOrder customOrder = CustomOrder.builder()
                            .uid(uid)
                            .bread(customOrderRequest.bread())
                            .material1(customOrderRequest.material1())
                            .material2(customOrderRequest.material2())
                            .material3(customOrderRequest.material3())
                            .cheese(customOrderRequest.cheese())
                            .vegetable1(customOrderRequest.vegetable1())
                            .vegetable2(customOrderRequest.vegetable2())
                            .vegetable3(customOrderRequest.vegetable3())
                            .vegetable4(customOrderRequest.vegetable4())
                            .vegetable5(customOrderRequest.vegetable5())
                            .vegetable6(customOrderRequest.vegetable6())
                            .vegetable7(customOrderRequest.vegetable7())
                            .vegetable8(customOrderRequest.vegetable8())
                            .sauce1(customOrderRequest.sauce1())
                            .sauce2(customOrderRequest.sauce2())
                            .sauce3(customOrderRequest.sauce3())
                            .build();
                    return customOrderRepository.save(customOrder);
                });
    }
}
