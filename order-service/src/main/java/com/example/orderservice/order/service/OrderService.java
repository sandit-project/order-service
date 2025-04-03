package com.example.orderservice.order.service;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.domain.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.orderservice.order.domain.OrderStatus.*;
import static org.springframework.http.HttpStatus.*;

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

    public Mono<Order> submitOrder(OrderRequestDTO orderRequestDTO) {
        Mono<Void> validateItems = Flux.fromIterable(orderRequestDTO.getItems())
                .flatMap(item ->
                        menuClientAdapter.validateMenuName(item.menuName())
                                .flatMap(isValid -> {
                                    log.info("validate result: {}, for item: {}", isValid, item.menuName());
                                    if (!isValid) {
                                        return Mono.error(new ResponseStatusException(
                                                BAD_REQUEST, "유효하지 않은 메뉴: " + item.menuName()));
                                    }
                                    return Mono.empty();
                                })
                ).then();

        return validateItems.then(Mono.defer(() -> {
            //List에 담긴 아이템을 직렬화
            String itemsJson;
            try {
                itemsJson = objectMapper.writeValueAsString(orderRequestDTO.getItems());
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException("CartItem JSON 직렬화 실패", e));
            }

            Order order = Order.builder()
                    .userUid(orderRequestDTO.getUserUid())
                    .socialUid(orderRequestDTO.getSocialUid())
                    .payment(orderRequestDTO.getPayment())
                    .merchantUid(orderRequestDTO.getMerchantUid())
                    .status(PAYMENT_PENDING)
                    .items(itemsJson)
                    .address(orderRequestDTO.getAddress())
                    .amount(orderRequestDTO.getItems().stream()
                            .mapToInt(item -> item.amount() * item.amount())
                            .sum())
                    .price(orderRequestDTO.getItems().stream()
                            .mapToInt(item -> item.price() * item.amount())
                            .sum())
                    .calorie(
                            orderRequestDTO.getItems().stream()
                                    .mapToDouble(item -> item.calorie() * item.amount())
                                    .sum()
                    )
                    .createdDate(LocalDateTime.now())
                    .build();

            return orderRepository.save(order);
        }));
    }

    public OrderResponseDTO toResponse(Order order) {
        List<CartItem> items = List.of(); // 기본값

        try {
            items = objectMapper.readValue(order.items(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("items 역직렬화 실패: {}", e.getMessage());
        }

        return OrderResponseDTO.builder()
                .uid(order.uid())
                .userUid(order.userUid())
                .payment(order.payment())
                .merchantUid(order.merchantUid())
                .items(items)
                .amount(order.amount())
                .address(order.address())
                .price(order.price())
                .calorie(order.calorie())
                .createdDate(order.createdDate())
                .status(order.status().name())
                .build();
    }
}
