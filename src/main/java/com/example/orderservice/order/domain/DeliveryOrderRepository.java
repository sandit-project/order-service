package com.example.orderservice.order.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class DeliveryOrderRepository {
    private final DatabaseClient databaseClient;

    public Flux<DeliveryOrderResponseDTO> getCookingOrders() {
        return databaseClient.sql("""
            SELECT
                o.uid AS uid,
                o.user_uid AS userUid,
                o.social_uid AS socialUid,
                o.store_uid AS storeUid,
                o.merchant_uid AS merchantUid,
                o.menu_name AS menuName,
                o.amount AS amount,
                o.price AS price,
                o.status AS status,
                o.created_date AS createdDate,
                o.reservation_date AS reservationDate,
                da.address_start AS addressStart,
                da.address_start_lat AS addressStartLat,
                da.address_start_lan AS addressStartLan,
                da.address_destination AS addressDestination,
                da.address_destination_lat AS addressDestinationLat,
                da.address_destination_lan AS addressDestinationLan
            FROM delivery_address da
            LEFT JOIN orders o ON da.merchant_uid = o.merchant_uid
            WHERE o.status = 'ORDER_COOKING'
            ORDER BY o.created_date ASC
        """)
                .map((row, meta) -> {
                    DeliveryOrderResponseDTO dto = new DeliveryOrderResponseDTO();
                    dto.setUid(row.get("uid", Long.class));
                    dto.setUserUid(row.get("userUid", Long.class));
                    dto.setSocialUid(row.get("socialUid", Long.class));
                    dto.setStoreUid(row.get("storeUid", Long.class));
                    dto.setMerchantUid(row.get("merchantUid", String.class));
                    dto.setMenuName(row.get("menuName", String.class));
                    dto.setAmount(row.get("amount", Integer.class));
                    dto.setPrice(row.get("price", Long.class));
                    dto.setStatus(row.get("status", String.class));
                    dto.setCreatedDate(row.get("createdDate", LocalDateTime.class));
                    dto.setReservationDate(row.get("reservationDate", LocalDateTime.class));
                    dto.setAddressStart(row.get("addressStart", String.class));
                    dto.setAddressStartLat(row.get("addressStartLat", Double.class));
                    dto.setAddressStartLan(row.get("addressStartLan", Double.class));
                    dto.setAddressDestination(row.get("addressDestination", String.class));
                    dto.setAddressDestinationLat(row.get("addressDestinationLat", Double.class));
                    dto.setAddressDestinationLan(row.get("addressDestinationLan", Double.class));
                    return dto;
                })
                .all();
    }

    public Flux<DeliveryOrderResponseDTO> getDeliveringOrders() {
        return databaseClient.sql("""
            SELECT
                o.uid AS uid,
                o.user_uid AS userUid,
                o.social_uid AS socialUid,
                o.store_uid AS storeUid,
                o.merchant_uid AS merchantUid,
                o.menu_name AS menuName,
                o.amount AS amount,
                o.price AS price,
                o.status AS status,
                o.created_date AS createdDate,
                o.reservation_date AS reservationDate,
                da.address_start AS addressStart,
                da.address_start_lat AS addressStartLat,
                da.address_start_lan AS addressStartLan,
                da.address_destination AS addressDestination,
                da.address_destination_lat AS addressDestinationLat,
                da.address_destination_lan AS addressDestinationLan
            FROM delivery_address da
            LEFT JOIN orders o ON da.merchant_uid = o.merchant_uid
            WHERE o.status = 'ORDER_DELIVERING'
            ORDER BY o.created_date ASC
        """)
                .map((row, meta) -> {
                    DeliveryOrderResponseDTO dto = new DeliveryOrderResponseDTO();
                    dto.setUid(row.get("uid", Long.class));
                    dto.setUserUid(row.get("userUid", Long.class));
                    dto.setSocialUid(row.get("socialUid", Long.class));
                    dto.setStoreUid(row.get("storeUid", Long.class));
                    dto.setMerchantUid(row.get("merchantUid", String.class));
                    dto.setMenuName(row.get("menuName", String.class));
                    dto.setAmount(row.get("amount", Integer.class));
                    dto.setPrice(row.get("price", Long.class));
                    dto.setStatus(row.get("status", String.class));
                    dto.setCreatedDate(row.get("createdDate", LocalDateTime.class));
                    dto.setReservationDate(row.get("reservationDate", LocalDateTime.class));
                    dto.setAddressStart(row.get("addressStart", String.class));
                    dto.setAddressStartLat(row.get("addressStartLat", Double.class));
                    dto.setAddressStartLan(row.get("addressStartLan", Double.class));
                    dto.setAddressDestination(row.get("addressDestination", String.class));
                    dto.setAddressDestinationLat(row.get("addressDestinationLat", Double.class));
                    dto.setAddressDestinationLan(row.get("addressDestinationLan", Double.class));
                    return dto;
                })
                .all();
    }
}
