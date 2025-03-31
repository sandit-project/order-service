package com.example.orderservice.order.domain;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final StreamBridge streamBridge;
    private final MenuClientAdapter menuClientAdapter;

    public Flux<Order> findAll() {
        return orderRepository.findAll();
    }

    public Mono<Order> submitOrder(Order order) {

        return menuClientAdapter.getMenuByUid(order.menuUid())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("menu not found" + order.menuUid())))
                .flatMap(menu -> {
                    Order orderToSave = Order.builder()
                            .menuUid(order.menuUid())
                            .amount(order.amount())
                            .price(order.price())
                            .calorie(order.calorie())
                            .build();
                    return orderRepository.save(orderToSave);
                })
                .doOnNext( savedOrder -> {
                            OrderCreatedMessage message = new OrderCreatedMessage(savedOrder.uid(), savedOrder.status());
                            streamBridge.send("orderCreated-out-0", message);
                        });

    }
}
