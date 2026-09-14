package com.example.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;                     // ✅ ADDED
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_transaction", columnList = "transactionId"),
        @Index(name = "idx_payment_external", columnList = "externalId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅✅✅ THE FIX — breaks Order.payment → Payment.order → Order.payment → … ✅✅✅
    // @JsonIgnore  → Jackson won't follow payment.order back to the Order
    // @ToString.Exclude / @EqualsAndHashCode.Exclude → Lombok won't follow it either
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(unique = true)
    private String transactionId;

    @Column(unique = true, nullable = false)
    private String externalId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.PaymentStatus status;

    @Column(columnDefinition = "TEXT")
    private String responseMessage;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}