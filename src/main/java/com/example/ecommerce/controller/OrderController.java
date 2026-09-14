package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Order> createOrder(@Valid @RequestBody CheckoutRequest request,
                                          HttpSession session) {
        Order order = orderService.createOrderFromCart(session.getId(), request);
        return ApiResponse.success("Order created", order);
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ApiResponse<Order> getByNumber(@PathVariable String orderNumber) {
        return ApiResponse.success(orderService.getOrderByNumber(orderNumber));
    }
}