package com.example.orderservice.rabbit;

import com.example.orderservice.event.OrderCreatedMessage;
import com.example.orderservice.order.domain.OrderStatus;
import com.example.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStreamListener {

    private final OrderService orderService;
    private final StreamBridge streamBridge;


    @Bean
    public Consumer<OrderCreatedMessage> acceptOrder() {
        return message -> {
            log.info("Order Message 수신: {}", message);

            // 여기서 먼저 거르기
            if (message.status() == OrderStatus.ORDER_CREATED) {
                log.info("status가 CREATED면 MQ 재발행 안함");
                return;
            }

            if (message.republished()) {
                log.info("이미 재발행된 메시지임. 다시 발행 안함");
                return;
            }

            orderService.saveOrderFromMessage(message)
                    .doOnSuccess(unused -> {
                        // 여기서는 publish만
                        OrderCreatedMessage republished = new OrderCreatedMessage(
                                message.merchantUid(),
                                message.userUid(),
                                message.socialUid(),
                                message.deliveryManUid(),
                                message.deliveryManType(),
                                message.storeUid(),
                                message.deliveryAddress(),
                                message.items(),
                                message.status(),
                                message.createdDate(),
                                true
                        );

                        boolean result = streamBridge.send("orderCreated-out-0",
                                MessageBuilder.withPayload(republished).build());

                        if (!result) {
                            log.error("MQ 발행 실패");
                        }
                    })
                    .doOnError(error -> log.error("Order Message 처리 실패", error))
                    .subscribe();
        };
    }

}
