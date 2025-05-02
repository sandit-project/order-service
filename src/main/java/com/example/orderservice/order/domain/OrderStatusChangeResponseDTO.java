package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderStatusChangeResponseDTO {
        private boolean success;
        private String message;
        private String merchantUid;
        private OrderStatus newStatus;
}
