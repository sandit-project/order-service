package com.example.orderservice.order.service;

import com.example.orderservice.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final OrderService orderService;

    public Flux<CustomOrder> findAllOrders() {
        return customOrderRepository.findAll();
    }

    public Mono<CustomOrder> findByUid(Integer uid) {
        return customOrderRepository.findById(uid);
    }

    public Mono<OrderResponseDTO> submitCustomOption(CustomOrderRequestDTO dto) {
        // 필수 검증
        if (dto.getBread() == null
                || dto.getMaterial1() == null
                || dto.getVegetable1() == null
                || dto.getSauce1() == null) {
            return Mono.just(OrderResponseDTO.builder()
                    .success(false)
                    .message("필수 옵션(bread, material1, vegetable1, sauce1) 누락")
                    .build());
        }

        // save → customOptionUid 리턴
        return customOrderRepository.save(CustomOrder.from(dto))
                .map(savedOpt -> OrderResponseDTO.builder()
                        .success(true)
                        .message("커스텀 옵션 저장 완료")
                        .orderUid(savedOpt.uid())   // customOptionUid 용으로 사용
                        .build()
                );
    }

    /** 2. 최종 주문 + 옵션 연동 */
    public Mono<OrderResponseDTO> submitFinalOrder(FinalCustomOrderRequest finalReq) {
        return orderService.submitOrder(finalReq.getOrderRequestDTO())
                .flatMap(orderResp -> {
                    // 주문 자체가 실패했다면 바로 실패 DTO 리턴
                    if (!orderResp.isSuccess()) {
                        return Mono.just(orderResp);
                    }

                    Integer orderUid = orderResp.getOrderUid();
                    if (orderUid == null) {
                        return Mono.just(OrderResponseDTO.builder()
                                .success(false)
                                .message("주문 UID 누락")
                                .build());
                    }

                    // 옵션 테이블에서 프리뷰 레코드 찾아서 → 주문 PK로 uid 덮어쓰기 → save
                    return customOrderRepository.findById(orderUid)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("저장된 옵션을 찾을 수 없습니다")))
                            .flatMap(opt -> {
                                // 주문 PK(orderUid)를 custom_order.uid 로 사용
                                return customOrderRepository.save(
                                        CustomOrder.builder()
                                                .uid(orderResp.getOrderUid())   // orders.uid 와 동일하게 덮어쓰기
                                                .bread(opt.bread())
                                                .material1(opt.material1())
                                                .material2(opt.material2())
                                                .material3(opt.material3())
                                                .cheese(opt.cheese())
                                                .vegetable1(opt.vegetable1())
                                                .vegetable2(opt.vegetable2())
                                                .vegetable3(opt.vegetable3())
                                                .vegetable4(opt.vegetable4())
                                                .vegetable5(opt.vegetable5())
                                                .vegetable6(opt.vegetable6())
                                                .vegetable7(opt.vegetable7())
                                                .vegetable8(opt.vegetable8())
                                                .sauce1(opt.sauce1())
                                                .sauce2(opt.sauce2())
                                                .sauce3(opt.sauce3())
                                                .version(opt.version())
                                                .build()
                                );
                            })
                            .thenReturn(OrderResponseDTO.builder()
                                    .success(true)
                                    .message("커스텀 주문 완료")
                                    .orderUid(orderResp.getOrderUid()) // 최종 주문 PK
                                    .build()
                            );
                })
                .onErrorResume(e -> Mono.just(
                        OrderResponseDTO.builder()
                                .success(false)
                                .message("최종 주문 실패: " + e.getMessage())
                                .build()
                ));
    }

    private boolean isNull(Integer value) {
        return value == null;
    }

}
