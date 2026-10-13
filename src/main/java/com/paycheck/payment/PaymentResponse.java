package com.paycheck.payment;

import java.math.BigDecimal;
import java.time.Instant;

/** What the API returns for a payment. */
public record PaymentResponse(
        Long id,
        String cardNumber,
        BigDecimal amount,
        String country,
        PaymentStatus status,
        String reason,
        Instant createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMaskedCard(),
                payment.getAmount(),
                payment.getCountry(),
                payment.getStatus(),
                payment.getReason(),
                payment.getCreatedAt());
    }
}
