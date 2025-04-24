package com.example.orderservice.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final MenuClient menuClient;

    public List<StoreResponseDTO> getAllStores() {
        return menuClient.getStores();
    }
}
