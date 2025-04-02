package com.example.orderservice.order.service;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequest;
import com.example.orderservice.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    //private final StreamBridge streamBridge;
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

    public Mono<Order> submitOrder(OrderRequest orderRequest) {
        // 1. 모든 CartItem의 메뉴명을 검증: 하나라도 유효하지 않으면 에러 발생
        Mono<Void> validateItems = Flux.fromIterable(orderRequest.items())
                .flatMap(item ->
                        menuClientAdapter.validateMenuName(item.menuName())
                                .flatMap(isValid -> {
                                    if (!isValid) {
                                        return Mono.error(new IllegalArgumentException(
                                                "유효하지 않은 메뉴: " + item.menuName()));
                                    }
                                    return Mono.empty();
                                })
                )
                .then();

        // 2. 검증이 완료되면 Order를 생성 및 저장 후, 각 CartItem을 OrderItem으로 매핑하여 저장
        return validateItems.then(Mono.defer(() -> {
            // Order 생성 (여기서는 OrderRequest의 공통 필드를 사용)
            Order order = Order.builder()
                    .userUid(orderRequest.userUid())
                    .socialUid(orderRequest.socialUid())
                    .payment(orderRequest.payment())
                    .merchantUid(orderRequest.merchantUid())
                    // 기타 Order 관련 필드들...
                    .build();
            return orderRepository.save(order);
        })).flatMap(savedOrder -> {
            // 3. 각 CartItem을 OrderItem으로 변환
            List<OrderItem> orderItems = orderRequest.items().stream()
                    .map(item -> OrderItem.builder()
                            .orderId(savedOrder.uid()) // 주문의 pk를 외래키로 사용
                            .menuName(item.menuName())
                            .amount(item.amount())
                            .price(item.price())
                            .calorie(item.calorie())
                            .build())
                    .collect(Collectors.toList());

            // 4. OrderItem들을 저장하고, 최종적으로 저장된 Order를 반환
            return orderItemRepository.saveAll(orderItems)
                    .thenReturn(savedOrder);
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
