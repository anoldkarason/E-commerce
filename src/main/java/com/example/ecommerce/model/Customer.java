package com.example.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;                  // ✅ ADDED
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    private String address;

    private String city;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ THE FIX — breaks the Order → Customer → orders → Order → ... loop
    @JsonIgnore
    @ToString.Exclude                                                // ✅ ALSO ADDED (see "Why" below)
    @EqualsAndHashCode.Exclude                                       // ✅ ALSO ADDED (see "Why" below)
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}