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

    public Mono<OrderResponseDTO> submitCustomOrder(CustomOrderRequestDTO customOrderRequestDTO) {
        // 필수 값 검증
        if (isNull(customOrderRequestDTO.getBread()) ||
                isNull(customOrderRequestDTO.getMaterial1()) ||
                isNull(customOrderRequestDTO.getVegetable1()) ||
                isNull(customOrderRequestDTO.getSauce1())) {

            return Mono.just(OrderResponseDTO.builder()
                    .success(false)
                    .message("필수 커스텀 정보 누락: bread, material1, vegetable1, sauce1은 필수입니다.")
                    .build());
        }

        // 오직 커스텀 옵션만 저장
        CustomOrder customOrder = CustomOrder.builder()
                // 여기서는 최종 주문이 생성되기 전이므로 uid는 아직 할당되지 않음 (null 처리)
                .uid(null)
                .bread(customOrderRequestDTO.getBread())
                .material1(customOrderRequestDTO.getMaterial1())
                .material2(customOrderRequestDTO.getMaterial2())
                .material3(customOrderRequestDTO.getMaterial3())
                .cheese(customOrderRequestDTO.getCheese())
                .vegetable1(customOrderRequestDTO.getVegetable1())
                .vegetable2(customOrderRequestDTO.getVegetable2())
                .vegetable3(customOrderRequestDTO.getVegetable3())
                .vegetable4(customOrderRequestDTO.getVegetable4())
                .vegetable5(customOrderRequestDTO.getVegetable5())
                .vegetable6(customOrderRequestDTO.getVegetable6())
                .vegetable7(customOrderRequestDTO.getVegetable7())
                .vegetable8(customOrderRequestDTO.getVegetable8())
                .sauce1(customOrderRequestDTO.getSauce1())
                .sauce2(customOrderRequestDTO.getSauce2())
                .sauce3(customOrderRequestDTO.getSauce3())
                .build();

        // 커스텀 옵션만 저장하고, 저장 성공 시 "담기 완료" 메시지 반환
        return customOrderRepository.save(customOrder)
                .thenReturn(OrderResponseDTO.builder()
                        .success(true)
                        .message("커스텀 주문 옵션 저장 성공. (추후 주문 생성 시 연동 필요)")
                        .build());
    }

    private boolean isNull(Integer value) {
        return value == null;
    }

    public Mono<OrderResponseDTO> linkCustomOrder(Integer orderUid, CustomOrderRequestDTO customOrderRequestDTO) {
        // Order와 연동
        CustomOrder customOrder = CustomOrder.builder()
                .uid(orderUid)  // 최종 주문 생성 시점에 생성된 orders.uid를 사용
                .bread(customOrderRequestDTO.getBread())
                .material1(customOrderRequestDTO.getMaterial1())
                .material2(customOrderRequestDTO.getMaterial2())
                .material3(customOrderRequestDTO.getMaterial3())
                .cheese(customOrderRequestDTO.getCheese())
                .vegetable1(customOrderRequestDTO.getVegetable1())
                .vegetable2(customOrderRequestDTO.getVegetable2())
                .vegetable3(customOrderRequestDTO.getVegetable3())
                .vegetable4(customOrderRequestDTO.getVegetable4())
                .vegetable5(customOrderRequestDTO.getVegetable5())
                .vegetable6(customOrderRequestDTO.getVegetable6())
                .vegetable7(customOrderRequestDTO.getVegetable7())
                .vegetable8(customOrderRequestDTO.getVegetable8())
                .sauce1(customOrderRequestDTO.getSauce1())
                .sauce2(customOrderRequestDTO.getSauce2())
                .sauce3(customOrderRequestDTO.getSauce3())
                .build();

        return customOrderRepository.save(customOrder)
                .thenReturn(OrderResponseDTO.builder()
                        .success(true)
                        .message("커스텀 주문 옵션 연동 성공")
                        .build());
    }

}
