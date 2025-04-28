package com.example.orderservice.order.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
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
        @Valid
        private DeliveryAddressDTO deliveryAddress;
        @NotBlank(message = "payment must be defined")
        private String payment;
        @NotBlank(message = "merchantUid must be defined")
        private String merchantUid;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime reservationDate;
        private boolean paymentSuccess;
}
