package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class DeliveryAddressDTO {
    private String addressStart;
    private Double addressStartLat;
    private Double addressStartLan;
    private String addressDestination;
    private Double addressDestinationLat;
    private Double addressDestinationLan;
}
