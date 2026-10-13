package com.paycheck.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** How many payments this card has made since the given time. */
    long countByCardHashAndCreatedAtAfter(String cardHash, Instant since);
}
