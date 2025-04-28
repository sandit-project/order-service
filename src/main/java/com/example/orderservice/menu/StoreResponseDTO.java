package com.example.orderservice.menu;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponseDTO {
    private Integer uid;
    private String storeName;
    private String address;
}
