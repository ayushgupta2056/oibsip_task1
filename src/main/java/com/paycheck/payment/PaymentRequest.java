package com.paycheck.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/** The JSON body of POST /payments. */
public record PaymentRequest(

        @NotBlank
        @Pattern(regexp = "\\d{12,19}", message = "must be 12 to 19 digits")
        String cardNumber,

        @NotNull
        @DecimalMin(value = "0.01", message = "must be greater than 0")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{2}", message = "must be a 2-letter country code, e.g. IN")
        String country
) {
}
