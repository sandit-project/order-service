package com.example.orderservice.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Mono<Cart> addToCart(CartRequestDTO cartRequestDTO) {
        Cart cart = Cart.builder()
                .userUid(cartRequestDTO.getUserUid())
                .menuName(cartRequestDTO.getMenuName())
                .amount(cartRequestDTO.getAmount())
                .price(cartRequestDTO.getPrice())
                .calorie(cartRequestDTO.getCalorie())
                .build();
        return cartRepository.save(cart);
    }

    public Flux<Cart> findCartByUserUid(Integer userUid) {
        return cartRepository.findByUserUid(userUid);
    }
}
