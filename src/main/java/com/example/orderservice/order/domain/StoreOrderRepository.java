package com.example.orderservice.order.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StoreOrderRepository extends ReactiveCrudRepository<Order,Integer> {

    @Query("""
      SELECT
       o.uid,
       o.user_uid,
       o.store_uid,
       o.merchant_uid,
       o.menu_name,
       o.amount,
       o.price,
       o.calorie,
       o.payment,
       o.status,
       o.created_date,
       o.reservation_date,
       da.address_destination AS delivery_address
     FROM orders o
     LEFT JOIN delivery_address da
       ON o.merchant_uid = da.merchant_uid       
     WHERE o.store_uid = :storeUid
    """)
    Flux<OrderWithDelivery> findAllWithDelivery(@Param("storeUid") Integer storeUid);



    //상태별 조회
    @Query("""
        SELECT * FROM orders
        WHERE store_uid = :storeUid
        AND status = :status
        ORDER BY uid ASC 
        Limit :limit
    """)
    Flux<Order> findOrderByStoreUidAndStatus(@Param("storeUid") Integer storeUid,
                                             @Param("status") OrderStatus status,
                                             @Param("limit") int limit
    );


    @Query("""
          SELECT * 
          FROM `orders`
          WHERE store_uid = :storeUid
         """)
    Flux<Order> findAllOrders(@Param("storeUid") Integer storeUid);


    // 상태 변경
    @Modifying
    @Query("UPDATE orders SET status = :status WHERE merchant_uid = :merchantUid")
    Mono<Void> updateStatusByUid(@Param("merchantUid") Integer merchantUid, @Param("status") OrderStatus status);


}
