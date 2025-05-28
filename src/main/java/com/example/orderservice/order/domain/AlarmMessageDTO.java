package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlarmMessageDTO {
    private String merchantUid;
    private String eventType;
    private String message;
    private String sender;
    private Long receiverUid;
    private String receiverType;
}
