package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStreamListener {

    private final OrderService orderService;


    @Bean
    public Consumer<OrderCreatedMessage> acceptOrder() {
        return message -> {
            log.info("[acceptOrder] 주문 수신 완료: {}", message);


            //DB 저장
            orderService.saveOrderFromMessage(message)
                    .doOnSuccess(unused -> log.info("DB 저장 완료"))
                    .doOnError(error -> log.error("DB 저장 실패", error))
                    .subscribe();
        };
    }
}
