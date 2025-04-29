package com.example.orderservice.store;

import com.example.orderservice.order.domain.CartItem;

import java.time.LocalDateTime;
import java.util.List;

public class DummyStoreOrderData {
    public static List<StoreOrderResponseDTO> getDummyStoreOrders() {
        // record: CartItem 생성자 호출
        CartItem item1 = new CartItem(
                1,
                "햄치즈 샌드위치",
                2,
                5500,
                1000.3,
                0
        );

        CartItem item2 = new CartItem(
                2,
                "터키 베이컨 샌드위치",
                1,
                6500,
                1000.0,
                0
        );

        StoreOrderResponseDTO order1 = StoreOrderResponseDTO.builder()
                .uid(1001)
                .userUid(501)
                .storeUid(3)
                .merchantUid("1234567890")
                .items(List.of(item1, item2))
                .payment("CARD")
                .status("ACCEPTED")
                .createdDate(LocalDateTime.now().minusHours(2))
                .reservationDate(LocalDateTime.now().plusHours(1))
                .build();

        StoreOrderResponseDTO order2 = StoreOrderResponseDTO.builder()
                .uid(1002)
                .userUid(502)
                .storeUid(3)
                .merchantUid("1234567890")
                .items(List.of(item1))
                .payment("CASH")
                .status("PENDING")
                .createdDate(LocalDateTime.now().minusDays(1))
                .reservationDate(LocalDateTime.now().plusDays(1))
                .build();

        return List.of(order1, order2);
    }
}
