package com.example.ecommerce.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String externalId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String provider;
    private String status;
    private String message;
    private String orderNumber;
}