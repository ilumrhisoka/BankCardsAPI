package com.example.bankcards.service.card;

import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for encrypting, decrypting, and masking card numbers.
 * It utilizes {@link EncryptionUtil} for cryptographic operations and {@link CardMaskingUtil} for masking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardEncryptionService {

    private final EncryptionUtil encryptionUtil;

    /**
     * Encrypts a plain card number.
     *
     * @param plainCardNumber The unencrypted card number string.
     * @return The encrypted card number string.
     * @throws IllegalArgumentException if the plain card number is null, empty, or has an invalid format.
     * @throws RuntimeException if the encryption process fails.
     */
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

    /**
     * Decrypts an encrypted card number.
     *
     * @param encryptedCardNumber The encrypted card number string.
     * @return The decrypted (plain) card number string.
     * @throws IllegalArgumentException if the encrypted card number is null or empty.
     * @throws RuntimeException if the decryption process fails.
     */
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

    /**
     * Retrieves a masked version of an encrypted card number.
     * The card number is first decrypted and then masked.
     *
     * @param encryptedCardNumber The encrypted card number string.
     * @return The masked card number string (e.g., "************1234"). Returns "****" if decryption fails.
     */
    public String getMaskedCardNumber(String encryptedCardNumber) {
        try {
            String plainCardNumber = decryptCardNumber(encryptedCardNumber);
            return CardMaskingUtil.maskCardNumber(plainCardNumber);
        } catch (Exception e) {
            log.error("Failed to get masked card number", e);
            return "****";
        }
    }

    /**
     * Compares a plain card number with an encrypted card number to check if they match.
     * The encrypted card number is decrypted internally for comparison.
     *
     * @param plainCardNumber The plain card number string.
     * @param encryptedCardNumber The encrypted card number string.
     * @return {@code true} if the plain card number matches the decrypted encrypted card number, {@code false} otherwise.
     */
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
}