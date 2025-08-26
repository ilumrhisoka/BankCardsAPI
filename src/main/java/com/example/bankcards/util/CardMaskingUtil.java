package com.example.bankcards.util;

/**
 * Utility class for card number masking and validation using the Luhn algorithm.
 */
public class CardMaskingUtil {

    /**
     * Masks a card number, revealing only the last four digits.
     * If the card number is null or too short, it returns "****".
     * Non-digit characters are removed before masking.
     *
     * @param cardNumber the card number string to mask.
     * @return a masked card number string (e.g., "****1234").
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String cleaned = cardNumber.replaceAll("\\D", "");

        if (cleaned.length() < 4) {
            return "****";
        }

        return "****" + cleaned.substring(cleaned.length() - 4);
    }

    /**
     * Validates a card number using the Luhn algorithm.
     * Also checks if the card number is null or has an invalid length (13-19 digits).
     * Non-digit characters are removed before validation.
     *
     * @param cardNumber the card number string to validate.
     * @return {@code true} if the card number is valid according to Luhn algorithm and length, {@code false} otherwise.
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        String cleaned = cardNumber.replaceAll("\\D", "");

        if (cleaned.length() < 13 || cleaned.length() > 19) {
            return false;
        }

        return luhnCheck(cleaned);
    }

    /**
     * Performs the Luhn algorithm check on a given numeric string.
     * This is a private helper method used by {@link #isValidCardNumber(String)}.
     *
     * @param cardNumber the numeric string representing the card number.
     * @return {@code true} if the card number passes the Luhn check, {@code false} otherwise.
     */
    private static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}