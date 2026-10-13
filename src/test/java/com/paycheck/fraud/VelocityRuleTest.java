package com.paycheck.fraud;

import com.paycheck.common.CardUtils;
import com.paycheck.payment.PaymentRepository;
import com.paycheck.payment.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VelocityRuleTest {

    private static final String CARD = "4111111111111111";
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final VelocityRule rule = new VelocityRule(repository, fixedClock, 3);

    private final PaymentRequest request = new PaymentRequest(CARD, new BigDecimal("500"), "IN");

    @Test
    void declinesFourthPaymentWithinOneMinute() {
        when(repository.countByCardHashAndCreatedAtAfter(CardUtils.hash(CARD), NOW.minusSeconds(60)))
                .thenReturn(3L);

        assertThat(rule.check(request)).hasValue("More than 3 payments from this card in 1 minute");
    }

    @Test
    void allowsThirdPaymentWithinOneMinute() {
        when(repository.countByCardHashAndCreatedAtAfter(CardUtils.hash(CARD), NOW.minusSeconds(60)))
                .thenReturn(2L);

        assertThat(rule.check(request)).isEmpty();
    }
}
