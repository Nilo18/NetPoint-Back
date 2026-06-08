package com.netpoint.main.utils;

import com.netpoint.main.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;

@Slf4j
public class CardValidationUtils {

    // spacebs da dashebs shlis
    public static String normalize(String raw) {
        return raw.replaceAll("[\\s\\-]", "");
    }

    // amowmebs sigirdzes da ro ricxvebia
    public static void validateFormat(String normalized) {
        if (!normalized.matches("\\d{13,19}")) {
            throw new BadRequestException("Invalid card number format.");
        }
    }

    // Luhn algorithmi
    public static void validateLuhn(String normalized) {
        int sum = 0;
        boolean alternate = false;
        for (int i = normalized.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(normalized.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        if (sum % 10 != 0) {
            throw new BadRequestException("The card number entered doesn't look right. Please check the digits.");
        }
    }

    // Detects brand from IIN prefix
    public static String detectBrand(String normalized) {
        if (normalized.matches("^4\\d+")) return "visa";

        // Modernized Mastercard regex (handles 5-series and 2-series)
        if (normalized.matches("^(?:5[1-5]|2(?:22[1-9]|2[3-9]\\d|[3-6]\\d{2}|7[0-1]\\d|720))\\d+")) {
            return "mastercard";
        }

        return "unknown";
    }

    public static String extractLast4(String normalized) {
        return normalized.substring(normalized.length() - 4);
    }

    // amexistvis 4 cipri, sxvebistvis 3
    public static void validateCvc(String cvc, String brand) {
        if (cvc == null || !cvc.matches("\\d+")) {
            throw new BadRequestException("Invalid CVC.");
        }
        int expected = 3;
        if (cvc.length() != expected) {
            throw new BadRequestException("CVC must be " + expected + " digits for " + brand + ".");
        }
    }

    public static void validateExpiry(Short expMonth, Short expYear) {
        if (expMonth < 1 || expMonth > 12) {
            throw new BadRequestException("Invalid expiry month.");
        }
        String expYearString = expYear.toString();
        if (expYearString.length() != 4) {
            throw new BadRequestException("Expiry year must be 4 digits.");
        }
        YearMonth expiry = YearMonth.of(expYear, expMonth);
        log.info("The expiry is: {}", expiry);
        YearMonth current = YearMonth.now();
        log.info("current is: {}", current);
        if (expiry.isBefore(current)) {
            throw new BadRequestException("Card is expired.");
        }
    }
}