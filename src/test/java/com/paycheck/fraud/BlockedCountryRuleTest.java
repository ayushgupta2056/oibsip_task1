package com.paycheck.fraud;

import com.paycheck.payment.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlockedCountryRuleTest {

    private final BlockedCountryRule rule = new BlockedCountryRule(List.of("KP", "IR"));

    @Test
    void declinesBlockedCountryIgnoringCase() {
        PaymentRequest request = new PaymentRequest("4111111111111111", new BigDecimal("500"), "kp");

        assertThat(rule.check(request)).hasValue("Country KP is blocked");
    }

    @Test
    void allowsCountryNotOnTheList() {
        PaymentRequest request = new PaymentRequest("4111111111111111", new BigDecimal("500"), "IN");

        assertThat(rule.check(request)).isEmpty();
    }
}
