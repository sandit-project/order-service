package com.example.orderservice.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CancelPaymentRequestDTO {
    @NotBlank
    private String merchantUid;
}
