package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse getCart(String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String sessionId, CartItemRequest request) {
        Cart cart = getOrCreateCart(sessionId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock. Available: " + product.getStock());
        }

        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.getQuantity();
            if (newQty > product.getStock()) {
                throw new BusinessException("Insufficient stock. Available: " + product.getStock());
            }
            existingItem.setQuantity(newQty);
            existingItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.getItems().add(item);
            cartRepository.save(cart);
        }

        Cart updated = cartRepository.findBySessionId(sessionId).orElseThrow();
        return toResponse(updated);
    }

    @Override
    @Transactional
    public CartResponse updateItem(String sessionId, Long itemId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart(sessionId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity > item.getProduct().getStock()) {
            throw new BusinessException("Insufficient stock. Available: " + item.getProduct().getStock());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart updated = cartRepository.findBySessionId(sessionId).orElseThrow();
        return toResponse(updated);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String sessionId, Long itemId) {
        Cart cart = getOrCreateCart(sessionId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        Cart updated = cartRepository.findBySessionId(sessionId).orElseThrow();
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void clearCart(String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().sessionId(sessionId).build()
                ));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .productImage(i.getProduct().getImage())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .sessionId(cart.getSessionId())
                .items(items)
                .total(cart.getTotal())
                .totalItems(cart.getTotalItems())
                .build();
    }
}