package com.example.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;                     // ✅ ADDED
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber", unique = true),
        @Index(name = "idx_order_status", columnList = "orderStatus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    // ✅ NO @JsonIgnore here — we WANT the customer to appear in Order JSON.
    // Customer.orders carries @JsonIgnore, so the loop is broken on that side.
    @ToString.Exclude                                                  // ✅ ADDED (Lombok safety)
    @EqualsAndHashCode.Exclude                                         // ✅ ADDED (Lombok safety)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    // ✅ FIX — was EAGER; EAGER + Lombok @Data + JSON = recursion + performance killer.
    // Switched to LAZY, and added Lombok exclusions.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ✅ FIX — Payment has a back-reference to Order. Exclude from Lombok
    // so toString/equals/hashCode don't recurse.
    // Do NOT add @JsonIgnore here (you may want payment info in order JSON),
    // but you MUST add @JsonIgnore on Payment.order — see File 3 below.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    private String notes;

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

    public enum OrderStatus {
        PENDING, PAID, PAYMENT_FAILED, CANCELLED, COMPLETED
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }
}