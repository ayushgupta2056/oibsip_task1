package com.paycheck.fraud;

import com.paycheck.payment.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountRuleTest {

    private final HighAmountRule rule = new HighAmountRule(new BigDecimal("100000"));

    @Test
    void declinesAmountOverTheLimit() {
        PaymentRequest request = new PaymentRequest("4111111111111111", new BigDecimal("100000.01"), "IN");

        assertThat(rule.check(request)).hasValue("Amount is over the limit of 100000");
    }

    @Test
    void allowsAmountExactlyAtTheLimit() {
        PaymentRequest request = new PaymentRequest("4111111111111111", new BigDecimal("100000"), "IN");

        assertThat(rule.check(request)).isEmpty();
    }
}
