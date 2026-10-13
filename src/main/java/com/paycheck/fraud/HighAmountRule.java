package com.paycheck.fraud;

import com.paycheck.payment.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/** Declines payments above a maximum amount (default ₹1,00,000). */
@Component
public class HighAmountRule implements FraudRule {

    private final BigDecimal maxAmount;

    public HighAmountRule(@Value("${paycheck.fraud.max-amount:100000}") BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public Optional<String> check(PaymentRequest request) {
        if (request.amount().compareTo(maxAmount) > 0) {
            return Optional.of("Amount is over the limit of " + maxAmount.toPlainString());
        }
        return Optional.empty();
    }
}
