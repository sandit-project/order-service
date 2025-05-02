package com.example.orderservice.store;

import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StoreOrderRepository extends ReactiveCrudRepository<Order,Integer> {

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

    // 상태+커서 페이징
    @Query("""
    SELECT * FROM orders
    WHERE store_uid = :storeUid
      AND status = :status
      AND uid < :lastUid
    ORDER BY uid DESC
    LIMIT :limit
""")
    Flux<Order> findOrderByStoreUidAndStatusWithCursor(
            @Param("storeUid") Integer storeUid,
            @Param("status") OrderStatus status,
            @Param("lastUid") Integer lastUid,
            @Param("limit") int limit
    );

    /**
     * 지점별 최신 주문(limit개) 조회
     */

    @Query("""
        SELECT * FROM orders
        WHERE store_uid = :storeUid
        ORDER BY uid DESC 
        LIMIT :limit
        """)
    Flux<Order> findOrderByStoreUid(@Param("storeUid") Integer storeUid,
                                    @Param("limit") int limit);

    /**
     * 커서 페이징(uid < cursor)
     */
    @Query("""
        SELECT * FROM orders
        WHERE store_uid = :storeUid
        AND uid < :lastUid
        ORDER BY uid DESC 
        LIMIT :limit
   """)
    Flux<Order> findOrderByStoreUidWithCursor(@Param("storeUid") Integer storeUid,
                                             @Param("lastUid") Integer lastUid,
                                             @Param("limit") int limit);

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
