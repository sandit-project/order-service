package com.example.orderservice.order.service;

import com.example.orderservice.cart.CartRepository;
import com.example.orderservice.cart.CartService;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final static Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

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
        OrderStatus orderStatus = orderRequestDTO.isPaymentSuccess()
                ? OrderStatus.PAYMENT_COMPLETED
                : OrderStatus.PAYMENT_CANCELLED;

        return Flux.fromIterable(orderRequestDTO.getItems())
                .flatMap(item -> cartRepository.findById(item.cartUid())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않은 카트: " + item.cartUid())))
                        .flatMap(cart -> {
                            Order order = Order.builder()
                                    .userUid(orderRequestDTO.getUserUid())
                                    .menuName(cart.menuName())
                                    .amount(cart.amount())
                                    .price(cart.price())
                                    .calorie(cart.calorie())
                                    .payment(orderRequestDTO.getPayment())
                                    .merchantUid(orderRequestDTO.getMerchantUid())
                                    .status(orderStatus)
                                    .build();
                            return orderRepository.save(order);
                        })
                );
    }


    public Mono<PreparePaymentResponseDTO> preparePayment(PreparePaymentRequestDTO request) {
        Order order = Order.builder()
                .merchantUid(request.getMerchantUid())
                .menuName(request.getMenuName())
                .amount(1)
                .payment("card")
                .status(OrderStatus.PAYMENT_CANCELLED)
                .price(request.getTotalPrice())
                .calorie(0.0)
                .build();

        return orderRepository.save(order)
                .map(savedOrder -> PreparePaymentResponseDTO.builder()
                        .merchantUid(savedOrder.merchantUid())
                        .requestedAmount(savedOrder.price())
                        .message("사전 검증 및 저장 완료")
                        .build());
    }


}
