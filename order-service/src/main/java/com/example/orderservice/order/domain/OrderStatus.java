package com.example.orderservice.order.domain;

public enum OrderStatus {
    ORDER_CREATED,  //주문 생성 (결제 전)
    PAYMENT_PENDING,    //결제 버튼 누름, PG 요청 중
    PAYMENT_COMPLETED, //결제 완료
    PAYMENT_FAILED, //결제 실패, 주문은 남아있음
    ORDER_CONFIRMED, //가게가 주문 승인
    COOKING,    //조리 중
    COOKING_COMPLETED, //조리 완료
    DELIVERY_OUT,   //배달 중
    DELIVERED, //배달 완료
    ORDER_COMPLETED, //최종 완료, 리뷰 가능 등등
    ORDER_CANCELLED,    //사용자/가게가 주문 취소
    ORDER_FAILED,   //시스템적 실패 (ex. DB 오류 등)
}
