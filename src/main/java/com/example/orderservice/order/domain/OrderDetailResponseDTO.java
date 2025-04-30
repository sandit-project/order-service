package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
//주문 정보 확인 DTO
public class OrderDetailResponseDTO {
    private Integer uid;
    private Integer userUid;
    private Integer storeUid;
    private String merchantUid;
    private List<CartItemRequestDTO> items;
    private String payment;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime reservationDate;
}
