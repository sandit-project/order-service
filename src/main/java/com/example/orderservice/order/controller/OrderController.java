package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.Order;
import com.example.orderservice.order.service.OrderService;
import com.example.orderservice.payment.PreparePaymentRequestDTO;
import com.example.orderservice.payment.PreparePaymentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final StreamBridge streamBridge;

    @GetMapping
    public Mono<OrderDetailResponseDTO> getOrders() {
        return orderService.findAllOrders()
                .collectList()
                .map(this::convertToDetailDTO)
                .switchIfEmpty(Mono.empty());
    }

    @GetMapping("/merchant/{merchantUid}")
    public Mono<List<OrderDetailResponseDTO>> getOrdersByMerchantUid(@PathVariable String merchantUid) {
        return orderService.getOrderByMerchantUid(merchantUid)
                .map(this::convertToSingleDetailDTO)
                .collectList();
    }


    @GetMapping("/status/cooking")
    public Mono<List<DeliveryOrderResponseDTO>> getCookingOrders() {
        return orderService.getCookingOrders()
                .collectList();
    }

    @GetMapping("/status/delivering")
    public Mono<List<DeliveryOrderResponseDTO>> getDeliveringOrders() {
        return orderService.getDeliveringOrders()
                .collectList();
    }

    @GetMapping("/{uid}")
    public Mono<OrderDetailResponseDTO> getOrderByUid(@PathVariable Integer uid) {
        return orderService.getOrderByUid(uid)
                .map(order -> convertToDetailDTO(List.of(order)));
    }

    @GetMapping("/user/{userType}/{userUid}")
    public Mono<List<OrderDetailResponseDTO>> findAllByUserUid(
            @PathVariable(name = "userType") String userType,
            @PathVariable(name = "userUid") Integer userUid
    ) {
        log.info("findAllByUserUid var: {},{}", userType, userUid);
        return orderService.findAllByUserUid(userType, userUid)
                .map(this::convertToSingleDetailDTO)
                .collectList();
    }


    //결제 준비
    @PostMapping("/prepare")
    public Mono<PreparePaymentResponseDTO> preparePayment(@RequestBody PreparePaymentRequestDTO request) {
        return orderService.preparePayment(request);
    }

    //주문하기
    @PostMapping
    public Mono<OrderResponseDTO> submitOrder(@RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        log.info("orderRequestDTO: {}", orderRequestDTO);
        return orderService.submitOrder(orderRequestDTO);
    }

    //주문 상세 정보
    private OrderDetailResponseDTO convertToDetailDTO(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found");
        }

        Order firstOrder = orders.get(0);

        List<CartItemRequestDTO> items = orders.stream()
                .map(order -> new CartItemRequestDTO(
                        order.getUid(),
                        order.getMenuName(),
                        order.getAmount(),
                        order.getPrice(),
                        order.getCalorie(),
                        order.getVersion()
                ))
                .collect(Collectors.toList());

        return OrderDetailResponseDTO.builder()
                .uid(firstOrder.getUid())
                .userUid(firstOrder.getUserUid())
                .items(items)
                .merchantUid(firstOrder.getMerchantUid())
                .storeUid(firstOrder.getStoreUid())
                .payment(firstOrder.getPayment())
                .status(String.valueOf(firstOrder.getStatus()))
                .createdDate(firstOrder.getCreatedDate())
                .reservationDate(firstOrder.getReservationDate())
                .build();
    }

    private OrderDetailResponseDTO convertToSingleDetailDTO(Order order) {
        return OrderDetailResponseDTO.builder()
                .uid(order.getUid())
                .userUid(order.getUserUid())
                .storeUid(order.getStoreUid())
                .merchantUid(order.getMerchantUid())
                .items(List.of(new CartItemRequestDTO(
                        order.getUid(),
                        order.getMenuName(),
                        order.getAmount(),
                        order.getPrice(),
                        order.getCalorie(),
                        order.getVersion()
                )))
                .payment(order.getPayment())
                .status(order.getStatus().toString())
                .createdDate(order.getCreatedDate())
                .reservationDate(order.getReservationDate())
                .build();
    }

    @PostMapping("/update-success")
    public Mono<OrderResponseDTO> updateOrderStatusSuccess(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToSuccess(request.getMerchantUid())
                .thenReturn(OrderResponseDTO.builder()
                        .success(true)
                        .message("주문 완료!")
                        .build());
    }

    @PostMapping("/update-fail")
    public Mono<OrderResponseDTO> updateOrderStatusFail(@RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatusToFailed(request.getMerchantUid())
                .thenReturn(OrderResponseDTO.builder()
                        .success(false)
                        .message("주문 실패!")
                        .build());
    }

    /**
     * 지점 주문 목록 조회
     * 예시: GET /orders/store/3?status=PAYMENT_COMPLETED
     */
    @GetMapping("/store/{storeUid}")
    public Mono<StoreOrderListResponseDTO> getByStoreOrder(@PathVariable("storeUid") Integer storeUid,
                                                           @RequestParam(name = "status", required = false) String status
    ) {
        // 요청 파라미터 로깅
        log.info("▶ getByStoreOrder 호출 – storeUid: {}, status: {}", storeUid, status);


        return orderService
                .getStoreOrders(storeUid, status)
                // 주문 단일 DTO 스트림 단계 로깅 (디버그 레벨)
                .doOnNext(o -> log.debug("   ▶ 페이로드: merchantUid={}, menu={}, amount={}",
                        o.getMerchantUid(), o.getMenuName(), o.getAmount()))
                .collectList()
                // 전체 주문 개수 로깅
                .doOnNext(list -> log.info("   ▶ 조회된 주문 개수: {}", list.size()))
                .map(flatList -> {
                    // merchandUid 별로 그룹핑
                    Map<String, List<DeliveryOrderResponseDTO>> grouped =
                            flatList.stream()
                                    .collect(Collectors.groupingBy(DeliveryOrderResponseDTO::getMerchantUid));

                    log.info("   ▶ merchantUid 그룹 수: {}", grouped.size());

                    // grouped.entrySet()사용
                    List<StoreOrderResponseDTO> nested = grouped.entrySet().stream()
                            .map(entry -> {
                                String merchantUid = entry.getKey();
                                List<DeliveryOrderResponseDTO> orders = entry.getValue();
                                //첫 번째 주문 샘플에서 공통 필드 가져오기
                                DeliveryOrderResponseDTO sample = orders.get(0);
                                // DTO 빌드
                                StoreOrderResponseDTO dto = new StoreOrderResponseDTO();
                                dto.setMerchantUid(merchantUid);
                                dto.setUserUid(sample.getUserUid());
                                dto.setCreatedDate(sample.getCreatedDate());
                                dto.setReservationDate(sample.getReservationDate());
                                dto.setStatus(sample.getStatus());
                                dto.setAddressDestination(sample.getAddressDestination());
                                // items 채우기
                                List<StoreOrderResponseDTO.ItemResponse> items =
                                        orders.stream()
                                                .map(o -> new StoreOrderResponseDTO.ItemResponse(o.getMenuName(), o.getAmount()))
                                                .collect(Collectors.toList());
                                dto.setItems(items);

                                log.debug("   ▶ 생성된 StoreOrderResponseDTO – merchantUid={}, itemsCount={}",
                                        merchantUid, items.size());

                                return dto;
                            })
                            .collect(Collectors.toList());
                    // 최종 리턴
                    return new StoreOrderListResponseDTO(nested);

                });

    }

}

