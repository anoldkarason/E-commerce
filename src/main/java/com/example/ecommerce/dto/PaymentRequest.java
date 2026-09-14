package com.example.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Provider is required")
    private String provider;
}