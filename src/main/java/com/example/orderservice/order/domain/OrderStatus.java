package com.example.orderservice.order.domain;

public enum OrderStatus {
    ORDER_CREATED,        // 주문 생성 (결제 버튼 누르기 전)
    PAYMENT_COMPLETED,    // 결제 완료
    PAYMENT_FAILED,         // 결제 취소
    ORDER_CONFIRMED,      // 가게가 주문 수락
    ORDER_CANCELLED,      // 주문 취소
    ORDER_COOKING,        // 조리 중
    ORDER_DELIVERING,   // 배달 중
    ORDER_DELIVERED   // 배달 완료
}
