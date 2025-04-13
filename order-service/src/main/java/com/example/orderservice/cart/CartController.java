package com.example.orderservice.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Flux<Cart> getCartItems() {
        return cartService.getCartItems();
    }

    @PostMapping
    public Mono<Cart> addToCart(@RequestBody CartRequestDTO cartRequestDTO) {
        return cartService.addToCart(cartRequestDTO);
    }

    @PatchMapping("/{uid}/update-amount")
    public Mono<Void> updateCartAmount(@PathVariable Integer uid, @RequestBody UpdateCartAmountRequest request) {
        return cartService.updateAmount(uid, request.getAmount());
    }

    @DeleteMapping("/{uid}")
    public Mono<Void> deleteCartItem(@PathVariable Integer uid) {
        return cartService.deleteCartItem(uid);
    }

}
