package com.example.orderservice.order.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
//전체 주문 정보
public class OrderRequestDTO {

        private Integer userUid;
        private Integer socialUid;
        @NotNull(message = "store must be defined")
        private Integer storeUid;
        //주문할 상품들
        @NotEmpty(message = "You must order at least 1 item.")
        @Valid
        private List<CartItem> items;
        @NotBlank(message = "payment must be defined")
        private String payment;
        @NotBlank(message = "merchantUid must be defined")
        private String merchantUid;
        private boolean paymentSuccess;
}
