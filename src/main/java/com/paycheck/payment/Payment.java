package com.paycheck.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A payment saved in the database.
 * The full card number is never stored: we keep a SHA-256 hash (to spot repeat use
 * of the same card) and a masked version (to show to users).
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String cardHash;

    @Column(nullable = false)
    private String maskedCard;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 2)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    protected Payment() {
        // required by JPA
    }

    public Payment(String cardHash, String maskedCard, BigDecimal amount, String country,
                   PaymentStatus status, String reason, Instant createdAt) {
        this.cardHash = cardHash;
        this.maskedCard = maskedCard;
        this.amount = amount;
        this.country = country;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getCardHash() { return cardHash; }
    public String getMaskedCard() { return maskedCard; }
    public BigDecimal getAmount() { return amount; }
    public String getCountry() { return country; }
    public PaymentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
