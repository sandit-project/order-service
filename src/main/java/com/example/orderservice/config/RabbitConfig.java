package com.example.orderservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final ConnectionFactory connectionFactory;

    // 큐 1: 결제 완료 큐
    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue("order-created.order-service", true); // durable: true
    }

    // 큐 2: 주문 수락 큐
    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue("order-accepted.order-service", true);
    }

    // 큐 3: 주문 취소 큐
    @Bean
    public Queue orderCancelledQueue() {
        return new Queue("order-cancelled.order-service", true);
    }

    // 큐 4: 조리중 큐
    @Bean
    public Queue orderCookingQueue() {
        return new Queue("order-cooking.order-service", true);
    }

    // 큐 5: 배달 시작 큐
    @Bean
    public Queue orderDeliveringQueue() {
        return new Queue("order-delivering.order-service", true);
    }

    // 큐 6: 배달 완료 큐
    @Bean
    public Queue orderDeliveredQueue() {
        return new Queue("order-delivered.order-service", true);
    }

    // 공용 RabbitTemplate 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    // RabbitAdmin으로 큐 선언 보장
    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(paymentCompletedQueue());
        rabbitAdmin.declareQueue(orderConfirmedQueue());
        rabbitAdmin.declareQueue(orderCancelledQueue());
        rabbitAdmin.declareQueue(orderCookingQueue());
        rabbitAdmin.declareQueue(orderDeliveringQueue());
        rabbitAdmin.declareQueue(orderDeliveredQueue());
        return rabbitAdmin;
    }
}
