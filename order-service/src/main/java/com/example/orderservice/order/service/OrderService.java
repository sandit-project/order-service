package com.example.orderservice.order.service;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.domain.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final MenuClientAdapter menuClientAdapter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Flux<Order> findAllOrders() {
        return orderRepository.findAllOrders();
    }

    public Mono<Order> getOrderByUid(Integer uid) {
        return orderRepository.findById(uid);
    }

    public Flux<Order> findAllByUserUid(Integer userUid) {
        return orderRepository.findByUserUid(userUid);
    }

    public Flux<Order> submitOrder(OrderRequestDTO orderRequestDTO) {
        return Flux.fromIterable(orderRequestDTO.getItems())
                .flatMap(item -> menuClientAdapter.validateMenuName(item.menuName())
                        .flatMap(isValid -> {
                            if (!isValid) {
                                return Mono.error(new ResponseStatusException(
                                        BAD_REQUEST, "유효하지 않은 메뉴: " + item.menuName()));
                            }

                            Order order = Order.builder()
                                    .userUid(orderRequestDTO.getUserUid()) // 그냥 DTO에서 받은 숫자 쓰기
                                    .menuName(item.menuName())
                                    .amount(item.amount())
                                    .price(item.price())
                                    .calorie(item.calorie())
                                    .payment(orderRequestDTO.getPayment())
                                    .status(OrderStatus.PAYMENT_PENDING)
                                    .build();

                            return orderRepository.save(order);
                        }));
    }


}
