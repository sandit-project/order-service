package com.example.orderservice.order.domain;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Table("delivery_address")
public record DeliveryAddress(

        @Id
        Integer uid,
        @Column("user_uid")
        Integer userUid,
        @Column("social_uid")
        Integer socialUid,
        @Column("merchant_uid")
        String merchantUid,
        @Column("address_start")
        String addressStart,
        @Column("address_start_lat")
        double addressStartlat,
        @Column("address_start_lan")
        double addressStartlan,
        @Column("address_destination")
        String addressDestination,
        @Column("address_destination_lat")
        double addressDestinationLat,
        @Column("address_destination_lan")
        double addressDestinationLan
) {
}
