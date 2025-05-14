package com.example.orderservice.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CancelPaymentResponseDTO {
    @JsonProperty("isSuccess")
    private boolean isSuccess;
    private String message;
}
