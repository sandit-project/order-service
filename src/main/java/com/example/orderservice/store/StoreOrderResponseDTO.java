package com.example.orderservice.store;

import com.example.orderservice.order.domain.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
public class StoreOrderResponseDTO {
    private Integer uid;
    private Integer userUid;
    private Integer storeUid;
    private String merchantUid;
    private List<CartItem> items;
    private String payment;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime reservationDate;
}
