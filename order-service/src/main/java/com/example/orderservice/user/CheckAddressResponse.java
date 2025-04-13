package com.example.orderservice.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckAddressResponse {
    private boolean hasAddress;
}
