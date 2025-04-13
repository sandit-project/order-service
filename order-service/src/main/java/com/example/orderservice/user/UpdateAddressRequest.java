package com.example.orderservice.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAddressRequest {
    private String mainAddress;
    private String subAddress1;
    private String subAddress2;
}
