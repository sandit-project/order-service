package com.example.orderservice.order.service;

import com.example.orderservice.menu.MenuClient;
import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderRepository;
import com.example.orderservice.order.domain.OrderRequestDTO;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
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

        System.out.println("== submitOrder called ==");
        System.out.println("items: " + orderRequestDTO.getItems());
        System.out.println("paymentSuccess: " + orderRequestDTO.isPaymentSuccess());

        boolean paymentSuccess = orderRequestDTO.isPaymentSuccess();

        OrderStatus orderStatus = paymentSuccess
                ? OrderStatus.PAYMENT_COMPLETED
                : OrderStatus.PAYMENT_FAILED;

        return Flux.fromIterable(orderRequestDTO.getItems())
                .flatMap(item -> menuClientAdapter.validateMenuName(item.menuName())
                        .flatMap(isValid -> {
                            if (!isValid) {
                                return Mono.error(new ResponseStatusException(
                                        BAD_REQUEST, "유효하지 않은 메뉴: " + item.menuName()));
                            }

                            Order order = Order.builder()
                                    .userUid(orderRequestDTO.getUserUid()) // 그냥 DTO에서 받은 숫자 쓰기
                                    .merchantUid(orderRequestDTO.getMerchantUid())
                                    .menuName(item.menuName())
                                    .amount(item.amount())
                                    .price(item.price())
                                    .calorie(item.calorie())
                                    .payment(orderRequestDTO.getPayment())
                                    .status(orderStatus)
                                    .build();
                            System.out.println(order.toString());

                            return orderRepository.save(order);
                        }));
    }

    public Mono<PreparePaymentResponseDTO> preparePayment(PreparePaymentRequestDTO request) {
        log.info("Preparing payment and saving: merchantUid={}, totalPrice={}", request.getMerchantUid(), request.getTotalPrice());

        if (request.getTotalPrice() == null || request.getTotalPrice() <= 0) {
            return Mono.error(new IllegalArgumentException("결제 금액이 유효하지 않습니다."));
        }

////        // "준비 단계" 상태의 Order를 저장
////        Order order = Order.builder()
////                .merchantUid(request.getMerchantUid())
////                .payment(null) // 아직 결제 수단 미정
////                .status(OrderStatus.PAYMENT_PENDING) // 준비중
////                .price(request.getTotalPrice())
////                .build();
//
//        return orderRepository.save(order)
//                .map(savedOrder -> PreparePaymentResponseDTO.builder()
//                        .merchantUid(savedOrder.merchantUid())
//                        .requestedAmount(savedOrder.price())
//                        .message("사전 검증 및 저장 완료")
//                        .build());
//    }

        return Mono.just(
                PreparePaymentResponseDTO.builder()
                        .merchantUid(request.getMerchantUid())
                        .requestedAmount(request.getTotalPrice())
                        .message("사전 검증 완료")
                        .build()
        );
    }


}
