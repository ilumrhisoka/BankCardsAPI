package com.example.bankcards.util;

public class CardMaskingUtil {

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