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

    public Flux<DeliveryOrderResponseDTO> getDeliveringOrdersByUserUid(Integer paramUid) {
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
            WHERE o.status = 'ORDER_DELIVERING' AND o.user_uid = :paramUid
            ORDER BY o.created_date ASC
        """)
                .bind("paramUid", paramUid)
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

    public Flux<DeliveryOrderResponseDTO> getDeliveringOrdersBySocialUid(Integer paramUid) {
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
            WHERE o.status = 'ORDER_DELIVERING' AND o.social_uid = :paramUid
            ORDER BY o.created_date ASC
        """)
                .bind("paramUid", paramUid)
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
    /**
     * 특정 지점(storeUid)의 주문을, status(PAYMENT_COMPLETED 등) 기준으로 조회
     */
    public Flux<DeliveryOrderResponseDTO> getStoreOrdersByStatusAndStoreUid(Integer storeUid, String status) {
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
            FROM orders o
            LEFT JOIN delivery_address da
              ON da.merchant_uid = o.merchant_uid
            WHERE o.store_uid = :storeUid
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.created_date ASC
        """)
                .bind("storeUid", storeUid)
                .bind("status", status)
                .map((row, meta) -> {
                    DeliveryOrderResponseDTO dto = new DeliveryOrderResponseDTO();
                    dto.setUid(row.get("uid", Long.class));
                    dto.setUserUid(row.get("userUid", Long.class));
                    dto.setSocialUid(row.get("socialUid", Long.class));
                    dto.setStoreUid(Long.valueOf(row.get("storeUid", Integer.class)));
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
