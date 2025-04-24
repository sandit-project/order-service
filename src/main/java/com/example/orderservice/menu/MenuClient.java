package com.example.orderservice.menu;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "menuClient", url = "http://localhost:9002", fallback = MenuClientFallback.class)
public interface MenuClient {

    @GetMapping("/stores")
    List<StoreResponseDTO> getStores();
}
