package com.example.orderservice.order.domain;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.web.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.example.orderservice.order.domain.OrderStatus.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final StreamBridge streamBridge;
    private final MenuClientAdapter menuClientAdapter;

    //전체 주문 조회
    public Flux<Order> findAll() {
        return orderRepository.findAll();
    }

    //개별 주문 조회
    public Mono<Order> getOrderByUid(Integer uid) {
        return orderRepository.findById(uid);
    }

    public Flux<Order> findAllByUserUid(Integer userUid) {
        return orderRepository.findByUserUid(userUid);
    }

    public Mono<Order> submitOrder(@RequestBody Order order) {

        return menuClientAdapter.validateMenuName(order.menuName())
                .flatMap(valid -> {
                            if(!valid) {
                                return Mono.error(new IllegalArgumentException("The menu is not exist" + order.menuName()));
                            }

                            Order orderToSave = Order.builder()
                            .userUid(order.userUid())
                            .menuName(order.menuName())
                            .amount(order.amount())
                            .price(order.price())
                            .calorie(order.calorie())
                            .status(PAYMENT_COMPLETED)
                            .build();
                    return orderRepository.save(orderToSave);
                })
                .doOnNext( savedOrder -> {
                            OrderCreatedMessage message = new OrderCreatedMessage(savedOrder.uid(), savedOrder.status());
                            streamBridge.send("orderCreated-out-0", message);
            });

    }

    public Mono<Order> cancelOrder(Integer uid) {
        return orderRepository.findById(uid)
                .flatMap(order -> {
                    Order cancelledOrder = Order.builder()
                            .status(ORDER_CANCELLED)
                            .build();
                    return orderRepository.save(cancelledOrder);
                });
    }

    //상태 변경
    public Mono<Order> updateOrderStatus(Integer uid, OrderStatus status) {
        return orderRepository.findById(uid)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("order not found" + uid)))
                .flatMap(order -> {
                    Order updatedOrder = Order.builder()
                            .userUid(order.userUid())
                            .socialUid(order.socialUid())
                            .menuName(order.menuName())
                            .amount(order.amount())
                            .price(order.price())
                            .calorie(order.calorie())
                            .status(status)
                            .build();

                    return orderRepository.save(updatedOrder);
                });
    }

    //결제 성공 후 주문 완료로 상태 변경
    public Mono<Order> completeOrder(Integer uid) {
        return updateOrderStatus(uid, ORDER_COMPLETED);
    }

    //주문 접수로 상태 변경
    public Mono<Order> receiveOrder(Integer uid) {
        return updateOrderStatus(uid, ORDER_RECEIVED);
    }

    //조리 중
    public Mono<Order> startCooking(Integer uid) {
        return updateOrderStatus(uid, COOKING);
    }

    //조리 완료
    public Mono<Order> completeCooking(Integer uid) {
        return updateOrderStatus(uid, COOKING_COMPLETED);
    }

    //배달 시작
    public Mono<Order> startDelivery(Integer uid) {
        return updateOrderStatus(uid, DELIVERY_OUT);
    }

    //배달 완료
    public Mono<Order> completeDelivery(Integer uid) {
        return updateOrderStatus(uid, DELIVERED);
    }

    //주문 실패
    public Mono<Order> orderFailed(Integer uid) {
        return updateOrderStatus(uid, ORDER_FAILED);
    }
}
