package com.example.orderservice.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("delivery_address")
public class DeliveryAddress {

        @Id
        private Long uid;

        @Column("user_uid")
        private Long userUid;

        @Column("social_uid")
        private Long socialUid;

        @Column("merchant_uid")
        private String merchantUid;

        @Column("address_start")
        private String addressStart;

        @Column("address_start_lat")
        private Double addressStartLat;

        @Column("address_start_lan")
        private Double addressStartLan;

        @Column("address_destination")
        private String addressDestination;

        @Column("address_destination_lat")
        private Double addressDestinationLat;

        @Column("address_destination_lan")
        private Double addressDestinationLan;
}
