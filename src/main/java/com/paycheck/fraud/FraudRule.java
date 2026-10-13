package com.paycheck.fraud;

import com.paycheck.payment.PaymentRequest;

import java.util.Optional;

/**
 * Strategy pattern: every fraud check implements this one interface.
 * <p>
 * To add a new rule, create a new class that implements FraudRule and mark it
 * with @Component. Spring finds it automatically and the PaymentService runs it,
 * with no other code changes needed.
 */
public interface FraudRule {

    /**
     * @return a reason if the payment breaks this rule, or empty if it is fine
     */
    Optional<String> check(PaymentRequest request);
}
