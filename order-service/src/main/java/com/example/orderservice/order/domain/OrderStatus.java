package com.example.orderservice.order.domain;

public enum OrderStatus {
    ORDER_COMPLETED,    //주문 완료
    ORDER_RECEIVED,     // 주문 접수
    PAYMENT_PENDING,   //결제 준비
    PAYMENT_FAILED,     // 결제 실패
    PAYMENT_COMPLETED,  // 결제 완료
    COOKING,            // 조리 중
    COOKING_COMPLETED,  // 조리 완료
    DELIVERY_PREPARED,  // 배달 준비
    DELIVERY_OUT,       // 배달 중
    DELIVERED,         // 배달 완료
    ORDER_FAILED,      // 주문 실패
    ORDER_CANCELLED  //주문 취소
    
    //예약은 아직 안 넣음
}
