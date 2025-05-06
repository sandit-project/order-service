package com.example.orderservice.order.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryOrderResponseDTO {

    private Long uid;
    private Long userUid;
    private Long socialUid;
    private Long storeUid;
    private String merchantUid;
    private String menuName;
    private Integer amount;
    private Long price;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime reservationDate;

    private String addressStart;
    private Double addressStartLat;
    private Double addressStartLan;
    private String addressDestination;
    private Double addressDestinationLat;
    private Double addressDestinationLan;
}

