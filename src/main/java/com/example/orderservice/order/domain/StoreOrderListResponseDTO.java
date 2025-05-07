package com.example.orderservice.order.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreOrderListResponseDTO {

    private List<StoreOrderResponseDTO> storeOrderLists;

}
