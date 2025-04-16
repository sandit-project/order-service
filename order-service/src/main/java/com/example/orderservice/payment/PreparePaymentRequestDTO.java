package com.example.orderservice.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PreparePaymentRequestDTO {

    @NotBlank(message = "menuName must be defined")
    private String menuName;

    @NotNull(message = "totalPrice must be defined")
    private Integer totalPrice;

    @NotBlank(message = "merchantUid must be defined")
    private String merchantUid;

    private LocalDateTime reservationDate;

    @NotNull(message = "store must be defined")
    private Integer storeUid;

    @NotNull(message = "user must be defined")
    private Integer userUid;
}
