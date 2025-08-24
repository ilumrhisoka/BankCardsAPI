package com.example.bankcards.service.card;

import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardEncryptionService {

    private final EncryptionUtil encryptionUtil;

    public String encryptCardNumber(String plainCardNumber) {
        if (plainCardNumber == null || plainCardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }

        String cleanedCardNumber = plainCardNumber.replaceAll("[\\s-]", "");

        if (!CardMaskingUtil.isValidCardNumber(cleanedCardNumber)) {
            throw new IllegalArgumentException("Invalid card number format");
        }

        try {
            String encrypted = encryptionUtil.encrypt(cleanedCardNumber);
            log.debug("Card number encrypted successfully");
            return encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt card number", e);
            throw new RuntimeException("Failed to encrypt card number", e);
        }
    }

    public String decryptCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Encrypted card number cannot be null or empty");
        }

        try {
            String decrypted = encryptionUtil.decrypt(encryptedCardNumber);
            log.debug("Card number decrypted successfully");
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt card number", e);
            throw new RuntimeException("Failed to decrypt card number", e);
        }
    }

    public String getMaskedCardNumber(String encryptedCardNumber) {
        try {
            String plainCardNumber = decryptCardNumber(encryptedCardNumber);
            return CardMaskingUtil.maskCardNumber(plainCardNumber);
        } catch (Exception e) {
            log.error("Failed to get masked card number", e);
            return "****";
        }
    }

    public boolean matchesCardNumber(String plainCardNumber, String encryptedCardNumber) {
        if (plainCardNumber == null || encryptedCardNumber == null) {
            return false;
        }

        try {
            String cleanedPlainNumber = plainCardNumber.replaceAll("[\\s-]", "");
            return encryptionUtil.matches(cleanedPlainNumber, encryptedCardNumber);
        } catch (Exception e) {
            log.error("Failed to match card numbers", e);
            return false;
        }
    }

    public String getLastFourDigits(String encryptedCardNumber) {
        try {
            String plainCardNumber = decryptCardNumber(encryptedCardNumber);
            String cleaned = plainCardNumber.replaceAll("\\D", "");
            if (cleaned.length() >= 4) {
                return cleaned.substring(cleaned.length() - 4);
            }
            return "****";
        } catch (Exception e) {
            log.error("Failed to get last four digits", e);
            return "****";
        }
    }
}