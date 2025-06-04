package com.example.orderservice.order.controller;

import com.example.orderservice.order.domain.*;
import com.example.orderservice.order.model.DeliveryAddress;
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
    private final DeliveryAddressRepository deliveryAddressRepository;

    @GetMapping
    public Mono<OrderDetailResponseDTO> getOrders() {
        return orderService.findAllOrders()
                .collectList()
                .flatMap(this::convertToDetailDTO)
                .switchIfEmpty(Mono.empty());
    }

    @GetMapping("/merchant/{merchantUid}")
    public Flux<OrderDetailResponseDTO> getOrdersByMerchantUid(@PathVariable String merchantUid) {
        return orderService.getOrderByMerchantUid(merchantUid)
                .flatMap(this::convertToSingleDetailDTO); // flatMap으로
    }

    @GetMapping("/{uid}")
    public Mono<OrderDetailResponseDTO> getOrderByUid(@PathVariable Integer uid) {
        return orderService.getOrderByUid(uid)
                .flatMap(order -> convertToDetailDTO(List.of(order)));
    }

    @GetMapping("/user/{userType}/{userUid}")
    public Flux<OrderDetailResponseDTO> findAllByUserUid(
            @PathVariable(name = "userType") String userType,
            @PathVariable(name = "userUid") Integer userUid
    ) {
        log.info("findAllByUserUid var: {},{}", userType, userUid);
        return orderService.findAllByUserUid(userType, userUid)
                .flatMap(this::convertToSingleDetailDTO);
    }

    @GetMapping("/user/delivering/{userType}/{userUid}")
    public Mono<List<DeliveryOrderResponseDTO>> getDeliveringOrdersByUserUid(
            @PathVariable(name = "userType") String userType,
            @PathVariable(name = "userUid") Integer userUid
    ) {
        log.info("getDeliveringOrdersByUserUid var: {},{}", userType, userUid);
        return orderService.getDeliveringOrdersByUserUid(userType, userUid)
                .collectList();
    }

    //결제 준비
    @PostMapping("/prepare")
    public Mono<PreparePaymentResponseDTO> preparePayment(@RequestBody PreparePaymentRequestDTO request) {
        return orderService.preparePayment(request);
    }

    //주문하기
    @PostMapping("/{userType}")
    public Mono<OrderResponseDTO> submitOrder(@PathVariable(name = "userType") String userType,
                                              @RequestBody @Valid OrderRequestDTO orderRequestDTO) {
        log.info("userType: {}, orderRequestDTO: {}", userType, orderRequestDTO);
        return orderService.submitOrder(userType, orderRequestDTO);
    }

    //주문 상세 정보
    private Mono<OrderDetailResponseDTO> convertToDetailDTO(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found");
        }

        Order firstOrder = orders.get(0);

        return deliveryAddressRepository.findByMerchantUid(firstOrder.getMerchantUid())
                .defaultIfEmpty(new DeliveryAddress())  // 주소 없으면 빈 객체
                .map(addrEntity -> {
                    DeliveryAddressDTO addrDto = DeliveryAddressDTO.builder()
                            .addressStart(addrEntity.getAddressStart())
                            .addressStartLat(addrEntity.getAddressStartLat())
                            .addressStartLan(addrEntity.getAddressStartLan())
                            .addressDestination(addrEntity.getAddressDestination())
                            .addressDestinationLat(addrEntity.getAddressDestinationLat())
                            .addressDestinationLan(addrEntity.getAddressDestinationLan())
                            .build();

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
                            .deliveryAddress(addrDto)
                            .createdDate(firstOrder.getCreatedDate())
                            .reservationDate(firstOrder.getReservationDate())
                            .build();
                });
    }

    private Mono<OrderDetailResponseDTO> convertToSingleDetailDTO(Order order) {
        return deliveryAddressRepository.findByMerchantUid(order.getMerchantUid())
                .defaultIfEmpty(new DeliveryAddress()) // fallback
                .map(addrEntity -> {
                    DeliveryAddressDTO addrDto = DeliveryAddressDTO.builder()
                            .addressStart(addrEntity.getAddressStart())
                            .addressStartLat(addrEntity.getAddressStartLat())
                            .addressStartLan(addrEntity.getAddressStartLan())
                            .addressDestination(addrEntity.getAddressDestination())
                            .addressDestinationLat(addrEntity.getAddressDestinationLat())
                            .addressDestinationLan(addrEntity.getAddressDestinationLan())
                            .build();

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
                            .deliveryAddress(addrDto)
                            .createdDate(order.getCreatedDate())
                            .reservationDate(order.getReservationDate())
                            .build();
                });
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
                                dto.setSocialUid(sample.getSocialUid());
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

