package com.example.orderservice.menu;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuClientFallback implements MenuClient {
    @Override
    public List<StoreResponseDTO> getStores() {
        return List.of(
                StoreResponseDTO.builder()
                        .uid(1)
                        .storeName("스토어 1")
                        .address("서울시 강동구")
                        .build(),
                StoreResponseDTO.builder()
                        .uid(2)
                        .storeName("스토어 2")
                        .address("서울시 종로구")
                        .build()
        );
    }
}

