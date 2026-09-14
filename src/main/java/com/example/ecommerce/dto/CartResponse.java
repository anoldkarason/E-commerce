package com.example.ecommerce.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private String sessionId;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private int totalItems;
}