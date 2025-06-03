package com.example.orderservice.order.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreOrderResponseDTO {
    private String merchantUid;
    private Long   userUid;
    private Long   socialUid;
    private LocalDateTime createdDate;
    private LocalDateTime reservationDate;
    private String status;
    private String addressDestination;
    private List<ItemResponse> items = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemResponse{
        private String menuName;
        private int amount;
    }
}
