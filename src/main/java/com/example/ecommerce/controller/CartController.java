package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(HttpSession session) {
        return ApiResponse.success(cartService.getCart(session.getId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody CartItemRequest request,
                                             HttpSession session) {
        return ApiResponse.success("Item added to cart",
                cartService.addItem(session.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(@PathVariable Long itemId,
                                                @RequestParam Integer quantity,
                                                HttpSession session) {
        return ApiResponse.success(cartService.updateItem(session.getId(), itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> removeItem(@PathVariable Long itemId,
                                                HttpSession session) {
        return ApiResponse.success("Item removed",
                cartService.removeItem(session.getId(), itemId));
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(HttpSession session) {
        cartService.clearCart(session.getId());
        return ApiResponse.success("Cart cleared", null);
    }
}