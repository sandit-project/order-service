package com.example.orderservice.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StoreOrderListResponseDTO {
    private List<StoreOrderResponseDTO> storeOrderLists;
    private boolean lastPage;
    private Integer nextCursor;
}
