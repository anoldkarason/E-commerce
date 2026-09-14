package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String sessionId);
    CartResponse addItem(String sessionId, CartItemRequest request);
    CartResponse updateItem(String sessionId, Long itemId, Integer quantity);
    CartResponse removeItem(String sessionId, Long itemId);
    void clearCart(String sessionId);
}