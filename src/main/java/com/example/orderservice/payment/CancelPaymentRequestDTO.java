package com.example.orderservice.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String reason;
}
