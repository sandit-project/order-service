package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.DeliveryOrderResponseDTO;
import com.example.orderservice.order.domain.StoreOrderResponseDTO;
import com.example.orderservice.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testGetByStoreOrder_groupingAndMapping() {
        // given
        Integer storeUid = 1;
        String status = null;

        DeliveryOrderResponseDTO d1 = new DeliveryOrderResponseDTO();
        d1.setMerchantUid("m1");
        d1.setUserUid(100L);
        d1.setMenuName("Menu1");
        d1.setAmount(1);
        d1.setCreatedDate(LocalDateTime.of(2025, 5, 7, 12, 0));
        d1.setReservationDate(null);
        d1.setStatus("PAYMENT_COMPLETED");
        d1.setAddressDestination("Address1");

        DeliveryOrderResponseDTO d2 = new DeliveryOrderResponseDTO();
        d2.setMerchantUid("m1");
        d2.setUserUid(100L);
        d2.setMenuName("Menu2");
        d2.setAmount(2);
        d2.setCreatedDate(d1.getCreatedDate());
        d2.setReservationDate(d1.getReservationDate());
        d2.setStatus(d1.getStatus());
        d2.setAddressDestination(d1.getAddressDestination());

        when(orderService.getStoreOrders(storeUid, status))
                .thenReturn(Flux.just(d1, d2));

        // when
        var result = orderController.getByStoreOrder(storeUid, status);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    List<
                            StoreOrderResponseDTO> lists = response.getStoreOrderLists();
                    // grouped by single merchantUid -> one entry
                    assert lists.size() == 1;

                    StoreOrderResponseDTO entry = lists.get(0);
                    assert "m1".equals(entry.getMerchantUid());
                    assert entry.getUserUid() == 100;
                    assert entry.getAddressDestination().equals("Address1");

                    // items count and contents
                    assert entry.getItems().size() == 2;
                    boolean found1 = entry.getItems().stream()
                            .anyMatch(i -> "Menu1".equals(i.getMenuName()) && i.getAmount() == 1);
                    boolean found2 = entry.getItems().stream()
                            .anyMatch(i -> "Menu2".equals(i.getMenuName()) && i.getAmount() == 2);
                    assert found1 && found2;
                })
                .verifyComplete();
    }

    @Test
    void testGetByStoreOrder_withStatusFilter() {
        // given
        Integer storeUid = 2;
        String status = "DELIVERED";

        DeliveryOrderResponseDTO d = new DeliveryOrderResponseDTO();
        d.setMerchantUid("m2");
        d.setUserUid(200L);
        d.setMenuName("MenuX");
        d.setAmount(3);
        d.setCreatedDate(LocalDateTime.of(2025, 5, 8, 10, 0));
        d.setReservationDate(LocalDateTime.of(2025, 5, 9, 10, 0));
        d.setStatus(status);
        d.setAddressDestination("AddressX");

        when(orderService.getStoreOrders(storeUid, status))
                .thenReturn(Flux.just(d));

        // when
        var result = orderController.getByStoreOrder(storeUid, status);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    List<StoreOrderResponseDTO> lists = response.getStoreOrderLists();
                    assert lists.size() == 1;

                    StoreOrderResponseDTO entry = lists.get(0);
                    assert "m2".equals(entry.getMerchantUid());
                    assert entry.getStatus().equals(status);
                    assert entry.getItems().size() == 1;
                    assert entry.getItems().get(0).getMenuName().equals("MenuX");
                    assert entry.getItems().get(0).getAmount() == 3;
                })
                .verifyComplete();
    }
}