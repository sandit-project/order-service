package com.example.orderservice.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreparePaymentResponseDTO {
    private String merchantUid;
    private int version;
    private Integer requestedAmount;
    private String message;
}
