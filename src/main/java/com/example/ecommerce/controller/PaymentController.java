package com.example.ecommerce.controller;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            HttpSession session) {
        PaymentResponse response = paymentService.initiatePayment(session.getId(), request);
        return ApiResponse.success("Payment initiated", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable Long id) {
        return ApiResponse.success(paymentService.getPaymentById(id));
    }

    @PostMapping("/azam-pay/callback")
    public ResponseEntity<String> handleCallback(@RequestBody AzamPayCallback callback) {
        log.info("AzamPay callback received: {}", callback);
        try {
            paymentService.handleCallback(callback);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            log.error("Callback processing error", e);
            return ResponseEntity.ok("Callback received");
        }
    }
}