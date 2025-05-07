package com.example.orderservice.event;

import com.example.orderservice.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderCreatedMessage (
        String merchantUid,
        OrderStatus status,

        //배달 수락시간, 완료시간
        LocalDateTime deliveryAcceptTime,
        LocalDateTime deliveredTime,

        Long riderUserUid,
        Long riderSocialUid,
        String addressStart,
        String addressDestination

){
}
