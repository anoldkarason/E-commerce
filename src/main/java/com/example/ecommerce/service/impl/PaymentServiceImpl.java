package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.PaymentException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.payment.AzamPayService;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final List<String> SUPPORTED_PROVIDERS =
            List.of("Airtel", "Tigo", "Halopesa", "Azampesa", "M-Pesa");

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AzamPayService azamPayService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(String sessionId, PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getOrderStatus() == Order.OrderStatus.PAID) {
            throw new BusinessException("Order is already paid");
        }

        if (!SUPPORTED_PROVIDERS.contains(request.getProvider())) {
            throw new BusinessException("Unsupported provider. Supported: " + SUPPORTED_PROVIDERS);
        }

        // Check for existing pending payment to prevent duplicates
        Payment existing = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null && existing.getStatus() == Order.PaymentStatus.PENDING) {
            log.warn("Existing pending payment found for order {}", order.getId());
            // Allow retry but generate new external ID
        } else if (existing != null && existing.getStatus() == Order.PaymentStatus.SUCCESS) {
            throw new BusinessException("Payment already completed for this order");
        }

        String externalId = order.getOrderNumber() + "-" + UUID.randomUUID().toString().substring(0, 8);

        AzamPayRequest azamRequest = AzamPayRequest.builder()
                .accountNumber(normalizePhone(request.getPhoneNumber()))
                .amount(order.getTotalAmount().toPlainString())
                .currency("TZS")
                .externalId(externalId)
                .provider(request.getProvider())
                .callbackUrl(azamPayConfig().getCallbackUrl())
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .externalId(externalId)
                .amount(order.getTotalAmount())
                .currency("TZS")
                .provider(request.getProvider())
                .phoneNumber(request.getPhoneNumber())
                .status(Order.PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        try {
            AzamPayResponse response = azamPayService.initiatePayment(azamRequest);

            payment.setResponseMessage(response.getMessage());
            try {
                payment.setRawResponse(objectMapper.writeValueAsString(response));
            } catch (Exception ignored) {}

            if (response.getTransactionId() != null) {
                payment.setTransactionId(response.getTransactionId());
            }

            if (Boolean.FALSE.equals(response.getSuccess())) {
                payment.setStatus(Order.PaymentStatus.FAILED);
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                order.setOrderStatus(Order.OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);
            }

            paymentRepository.save(payment);

            return toResponse(payment, order, response.getMessage());
        } catch (PaymentException e) {
            payment.setStatus(Order.PaymentStatus.FAILED);
            payment.setResponseMessage(e.getMessage());
            paymentRepository.save(payment);
            throw e;
        }
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return toResponse(payment, payment.getOrder(), payment.getResponseMessage());
    }

    @Override
    @Transactional
    public void handleCallback(AzamPayCallback callback) {
        log.info("Received AzamPay callback: {}", callback);

        Payment payment = paymentRepository.findByExternalId(callback.getExternalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for externalId: " + callback.getExternalId()));

        // Idempotency: if already processed with same status, ignore
        if (payment.getStatus() == Order.PaymentStatus.SUCCESS
                && "success".equalsIgnoreCase(callback.getStatus())) {
            log.info("Duplicate success callback ignored for {}", callback.getExternalId());
            return;
        }

        if (callback.getTransactionId() != null) {
            payment.setTransactionId(callback.getTransactionId());
        }
        payment.setResponseMessage(callback.getMessage());
        try {
            payment.setRawResponse(objectMapper.writeValueAsString(callback));
        } catch (Exception ignored) {}

        Order order = payment.getOrder();

        String status = callback.getStatus() != null ? callback.getStatus().toLowerCase() : "";

        if ("success".equals(status) || "successful".equals(status) || "completed".equals(status)) {
            payment.setStatus(Order.PaymentStatus.SUCCESS);
            order.setPaymentStatus(Order.PaymentStatus.SUCCESS);
            order.setOrderStatus(Order.OrderStatus.PAID);

            // Reduce stock now that payment succeeded
            for (OrderItem item : order.getItems()) {
                Product p = item.getProduct();
                p.setStock(Math.max(0, p.getStock() - item.getQuantity()));
            }
        } else if ("failed".equals(status) || "error".equals(status)) {
            payment.setStatus(Order.PaymentStatus.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            order.setOrderStatus(Order.OrderStatus.PAYMENT_FAILED);
        } else {
            log.warn("Unknown callback status: {}", status);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
    }

    private String normalizePhone(String phone) {
        String p = phone.replaceAll("[^0-9]", "");
        if (p.startsWith("0")) return "255" + p.substring(1);
        if (p.startsWith("255")) return p;
        return p;
    }

    private PaymentResponse toResponse(Payment p, Order order, String message) {
        return PaymentResponse.builder()
                .paymentId(p.getId())
                .externalId(p.getExternalId())
                .transactionId(p.getTransactionId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .provider(p.getProvider())
                .status(p.getStatus().name())
                .message(message)
                .orderNumber(order.getOrderNumber())
                .build();
    }

    private com.example.ecommerce.config.AzamPayConfig azamPayConfig() {
        return azamPayConfig;
    }

    private final com.example.ecommerce.config.AzamPayConfig azamPayConfig;
}