package com.example.orderservice.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomCartRepository customCartRepository;

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

    public Flux<Cart> getCartItems() {
        return cartRepository.findAll();
    }

    @Transactional
    public Mono<Void> deleteCartItem(Integer uid) {
        return customCartRepository.deleteById(uid)  // ① custom_cart 먼저 삭제
                .then(cartRepository.deleteById(uid));        // ② cart 삭제
    }

    public Mono<Void> updateAmount(Integer uid, int amount) {
        return cartRepository.updateAmount(uid, amount);
    }

}
