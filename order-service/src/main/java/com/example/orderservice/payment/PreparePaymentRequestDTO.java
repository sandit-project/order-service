package com.example.orderservice.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreparePaymentRequestDTO {
    private String merchantUid;
    private Integer totalPrice;
}
