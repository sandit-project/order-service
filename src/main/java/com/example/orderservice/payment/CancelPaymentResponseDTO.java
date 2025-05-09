package com.example.orderservice.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CancelPaymentResponseDTO {
    private boolean success;
    private String message;
}
