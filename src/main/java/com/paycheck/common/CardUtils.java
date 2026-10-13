package com.paycheck.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Helpers so we never have to store or show a full card number. */
public final class CardUtils {

    private CardUtils() {
    }

    /** "4111111111111111" -> "************1111" */
    public static String mask(String cardNumber) {
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "*".repeat(cardNumber.length() - 4) + last4;
    }

    /** SHA-256 hash, used to recognise the same card without storing it. */
    public static String hash(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
