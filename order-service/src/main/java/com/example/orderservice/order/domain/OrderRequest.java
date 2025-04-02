package com.example.orderservice.order.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

//전체 주문 정보
public record OrderRequest (
        Integer userUid,
        Integer socialUid,
        //주문할 상품들
        @NotEmpty(message = "You must order at least 1 item.")
        @Valid
        List<CartItem> items,
        @NotBlank(message = "payment must be defined")
        String payment,
        @NotBlank(message = "address must be defined")
        String address,
        @NotBlank(message = "merchantUid must be defined")
        String merchantUid
) {
}
