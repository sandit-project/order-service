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

    //2. 최종 주문 + 옵션 연동
    public Mono<OrderResponseDTO> submitFinalOrder(FinalCustomOrderRequest finalReq) {
        Integer orderUid = finalReq.getOrderRequestDTO().getOrderUid();
        if (orderUid == null || orderUid <= 0) {
            return Mono.just(OrderResponseDTO.builder()
                    .success(false)
                    .message("유효하지 않은 주문 UID")
                    .orderUid(orderUid)
                    .build());
        }

        return customOrderRepository.save(
                CustomOrder.builder()
                        .uid(orderUid)
                        .bread(finalReq.getCustomOrderRequestDTO().getBread())
                        .material1(finalReq.getCustomOrderRequestDTO().getMaterial1())
                        .material2(finalReq.getCustomOrderRequestDTO().getMaterial2())
                        .material3(finalReq.getCustomOrderRequestDTO().getMaterial3())
                        .cheese(finalReq.getCustomOrderRequestDTO().getCheese())
                        .vegetable1(finalReq.getCustomOrderRequestDTO().getVegetable1())
                        .vegetable2(finalReq.getCustomOrderRequestDTO().getVegetable2())
                        .vegetable3(finalReq.getCustomOrderRequestDTO().getVegetable3())
                        .vegetable4(finalReq.getCustomOrderRequestDTO().getVegetable4())
                        .vegetable5(finalReq.getCustomOrderRequestDTO().getVegetable5())
                        .vegetable6(finalReq.getCustomOrderRequestDTO().getVegetable6())
                        .vegetable7(finalReq.getCustomOrderRequestDTO().getVegetable7())
                        .vegetable8(finalReq.getCustomOrderRequestDTO().getVegetable8())
                        .sauce1(finalReq.getCustomOrderRequestDTO().getSauce1())
                        .sauce2(finalReq.getCustomOrderRequestDTO().getSauce2())
                        .sauce3(finalReq.getCustomOrderRequestDTO().getSauce3())
                        .build()
        ).thenReturn(OrderResponseDTO.builder()
                .success(true)
                .message("커스텀 주문 저장 완료")
                .orderUid(orderUid)
                .build());
    }

    private boolean isNull(Integer value) {
        return value == null;
    }

}
