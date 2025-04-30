package com.example.orderservice.store;

import com.example.orderservice.order.domain.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StoreOrderRepository extends ReactiveCrudRepository<Order,Integer> {
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

}
