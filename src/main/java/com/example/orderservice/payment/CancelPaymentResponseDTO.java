package com.example.orderservice.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CancelPaymentResponseDTO {
    private boolean success;
    private String message;
}
