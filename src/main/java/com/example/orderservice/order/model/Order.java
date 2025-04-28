package com.example.orderservice.order.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Order {
        @Id
        private Integer uid;

        @Column("user_uid")
        private Integer userUid;

        @Column("social_uid")
        private Integer socialUid;

        @Column("store_uid")
        private Integer storeUid;

        @Column("merchant_uid")
        private String merchantUid;

        @Column("menu_name")
        private String menuName;

        @Column("amount")
        private Integer amount;

        @Column("price")
        private Integer price;

        @Column("calorie")
        private Double calorie;

        @Column("payment")
        private String payment;

        @Column("status")
        private String status; // enum 쓰면 @Enumerated(EnumType.STRING) 붙이든가

        @Column("created_date")
        private LocalDateTime createdDate;

        @Column("reservation_date")
        private LocalDateTime reservationDate;

        @Version
        private Integer version;
}