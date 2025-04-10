package com.example.orderservice;

import com.example.orderservice.order.controller.OrderController;
import com.example.orderservice.order.service.CustomOrderService;
import com.example.orderservice.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
public class CustomOrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CustomOrderService customOrderService;

    @Test
    void getOrders_returnsOk() {
        // given
        when(customOrderService.findAllOrders()).thenReturn(Flux.empty());

        // when + then
        webTestClient.get()
                .uri("/orders/custom")
                .exchange()
                .expectStatus().isOk();
    }
}
