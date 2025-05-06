package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class OrderResponseDTO {
    private boolean success;
    private String message;
    // 각 튜플의 uid
    private Integer orderUid;
    //동일한 merchant_uid에 해당하는 모든 order_uid 리스트
    private List<Integer> orderUids;
}
