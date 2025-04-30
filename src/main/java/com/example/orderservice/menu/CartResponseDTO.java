package com.example.orderservice.menu;

import com.example.orderservice.order.domain.CartItemRequestDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartResponseDTO {
    private int totalQuantity;  // 총 수량
    private long totalPrice;    // 총 가격
    private List<CartItemRequestDTO> cartItems;  // 장바구니 항목

    public List<CartItemRequestDTO> getCartItems() {
        return cartItems;
    }
}
