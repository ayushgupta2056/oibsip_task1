package com.paycheck.fraud;

import com.paycheck.payment.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Declines payments from countries on the blocked list. */
@Component
public class BlockedCountryRule implements FraudRule {

    private final Set<String> blockedCountries;

    public BlockedCountryRule(@Value("${paycheck.fraud.blocked-countries:KP,IR}") List<String> blockedCountries) {
        this.blockedCountries = blockedCountries.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> check(PaymentRequest request) {
        String country = request.country().toUpperCase();
        if (blockedCountries.contains(country)) {
            return Optional.of("Country " + country + " is blocked");
        }
        return Optional.empty();
    }
}
