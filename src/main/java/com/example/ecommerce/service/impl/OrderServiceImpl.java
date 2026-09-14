package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Order createOrderFromCart(String sessionId, CheckoutRequest request) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        // Validate stock for all items (fresh from DB)
        List<CartItem> validatedItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + product.getName()
                        + ". Available: " + product.getStock());
            }
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());
            validatedItems.add(item);
        }

        // Find or create customer
        Customer customer = customerRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> customerRepository.save(
                        Customer.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .phoneNumber(request.getPhoneNumber())
                                .address(request.getAddress())
                                .city(request.getCity())
                                .build()
                ));

        // Update customer details if provided
        if (request.getName() != null) customer.setName(request.getName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        customerRepository.save(customer);

        // Calculate total from DB prices only
        BigDecimal total = validatedItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .totalAmount(total)
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : validatedItems) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .subtotal(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build();
            orderItems.add(oi);
        }
        order.setItems(orderItems);
        orderRepository.save(order);

        // Clear cart after successful order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order created: {} with total {}", order.getOrderNumber(), total);
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }
}