package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPaymentMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;
    
    @Column(name = "card_number", length = 20)
    private String cardNumber;
    
    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;
    
    @Column(name = "expiry_date", length = 7)
    private String expiryDate;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum PaymentType {
        CREDIT_CARD, DEBIT_CARD, E_WALLET, BANK_TRANSFER
    }
}
