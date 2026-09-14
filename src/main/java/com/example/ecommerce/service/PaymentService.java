package com.example.ecommerce.service;

import com.example.ecommerce.dto.AzamPayCallback;
import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.model.Payment;

public interface PaymentService {
    PaymentResponse initiatePayment(String sessionId, PaymentRequest request);
    PaymentResponse getPaymentById(Long id);
    void handleCallback(AzamPayCallback callback);
    Payment getPaymentByOrderId(Long orderId);
}