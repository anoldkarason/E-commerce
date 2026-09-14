package com.example.ecommerce.service;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.model.Order;

public interface OrderService {
    Order createOrderFromCart(String sessionId, CheckoutRequest request);
    Order getOrderById(Long id);
    Order getOrderByNumber(String orderNumber);
}