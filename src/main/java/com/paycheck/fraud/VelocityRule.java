package com.paycheck.fraud;

import com.paycheck.common.CardUtils;
import com.paycheck.payment.PaymentRepository;
import com.paycheck.payment.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Declines a card that is used too often in a short time.
 * Default: more than 3 payments in 1 minute, so the 4th payment is declined.
 */
@Component
public class VelocityRule implements FraudRule {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final PaymentRepository repository;
    private final Clock clock;
    private final int maxPaymentsPerMinute;

    public VelocityRule(PaymentRepository repository,
                        Clock clock,
                        @Value("${paycheck.fraud.max-payments-per-minute:3}") int maxPaymentsPerMinute) {
        this.repository = repository;
        this.clock = clock;
        this.maxPaymentsPerMinute = maxPaymentsPerMinute;
    }

    @Override
    public Optional<String> check(PaymentRequest request) {
        Instant oneMinuteAgo = Instant.now(clock).minus(WINDOW);
        String cardHash = CardUtils.hash(request.cardNumber());
        long recentPayments = repository.countByCardHashAndCreatedAtAfter(cardHash, oneMinuteAgo);

        // recentPayments are the ones already made; this request would be one more
        if (recentPayments + 1 > maxPaymentsPerMinute) {
            return Optional.of("More than " + maxPaymentsPerMinute + " payments from this card in 1 minute");
        }
        return Optional.empty();
    }
}
