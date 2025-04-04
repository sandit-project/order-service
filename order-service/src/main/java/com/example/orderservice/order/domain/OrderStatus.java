package com.example.orderservice.order.domain;

public enum OrderStatus {
    ORDER_CREATED,        // 주문 생성 (결제 버튼 누르기 전)
    PAYMENT_PENDING,      // 결제 요청 (PG 요청 중)
    PAYMENT_COMPLETED,    // 결제 완료
    PAYMENT_CANCELLED,       // 결제 실패 (주문은 살아있음)
    ORDER_CONFIRMED,      // 가게가 주문 수락
    ORDER_CANCELLED,      // 주문 취소
    ORDER_COMPLETED       // 주문 완료 (배달 완료 + 리뷰 가능)
}
