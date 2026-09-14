package com.example.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+?255|0)[0-9]{9}$", message = "Invalid Tanzanian phone number")
    private String phoneNumber;

    @Email(message = "Invalid email")
    private String email;

    private String address;
    private String city;
    private String notes;
}