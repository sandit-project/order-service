package com.example.orderservice;

import com.example.orderservice.menu.MenuClientAdapter;
import com.example.orderservice.order.controller.OrderController;
import com.example.orderservice.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrders_returnsOk() {
        // given
        when(orderService.findAllOrders()).thenReturn(Flux.empty());

        // when + then
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }
}
