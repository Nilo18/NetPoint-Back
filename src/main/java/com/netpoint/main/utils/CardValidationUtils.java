package com.netpoint.main.utils;

import com.netpoint.main.exceptions.BadRequestException;

import java.time.YearMonth;

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
            throw new BadRequestException("Invalid card number (failed Luhn check).");
        }
    }

    // Detects brand from IIN prefix
    public static String detectBrand(String normalized) {
        if (normalized.matches("^4\\d+"))                          return "visa";
        if (normalized.matches("^5[1-5]\\d+"))                    return "mastercard";
        if (normalized.matches("^(34|37)\\d+"))                   return "amex";
        if (normalized.matches("^6(?:011|5\\d{2})\\d+"))          return "discover";
        if (normalized.matches("^3(?:0[0-5]|[68])\\d+"))          return "dinersclub";
        if (normalized.matches("^35(?:2[89]|[3-8]\\d)\\d+"))      return "jcb";
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
        int expected = "amex".equals(brand) ? 4 : 3;
        if (cvc.length() != expected) {
            throw new BadRequestException("CVC must be " + expected + " digits for " + brand + ".");
        }
    }

    public static void validateExpiry(Short expMonth, Short expYear) {
        if (expMonth < 1 || expMonth > 12) {
            throw new BadRequestException("Invalid expiry month.");
        }
        YearMonth expiry  = YearMonth.of(expYear, expMonth);
        YearMonth current = YearMonth.now();
        if (expiry.isBefore(current)) {
            throw new BadRequestException("Card is expired.");
        }
    }
}