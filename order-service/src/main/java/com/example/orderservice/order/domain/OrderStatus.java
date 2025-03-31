package com.example.orderservice.order.domain;

public enum OrderStatus {
    PAYMENT_FAILED,    // 결제 실패
    PAYMENT_COMPLETED, // 결제 완료
    ORDER_RECEIVED,    // 주문 접수
    COOKING,           // 조리 중
    COOKING_COMPLETED, // 조리 완료
    PREPARING_DELIVERY,// 배달 준비
    OUT_FOR_DELIVERY,  // 배달 중
    DELIVERED,         // 배달 완료
    ORDER_FAILED,       // 주문 실패
    ORDER_CANCELLED  //주문 취소
    
    //예약은 아직 안 넣음
}
