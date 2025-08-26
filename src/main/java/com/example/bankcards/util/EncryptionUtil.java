package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for performing AES/GCM encryption and decryption.
 * Uses a secret key configured via Spring properties.
 * Provides methods to encrypt, decrypt, and compare plain text with encrypted text.
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Value("${encryption.key}")
    private String encryptionKey;

    /**
     * Encrypts the given plain text using AES/GCM.
     * A random IV is generated for each encryption operation and prepended to the ciphertext.
     * The result is Base64 encoded.
     *
     * @param plainText the string to be encrypted.
     * @return the Base64 encoded encrypted string, including the IV.
     * @throws RuntimeException if an error occurs during encryption.
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // Генерируем случайный IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = new byte[iv.length + encryptedText.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedText, 0, encryptedWithIv, iv.length, encryptedText.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            log.error("Error during encryption: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts the given Base64 encoded encrypted text using AES/GCM.
     * The IV is extracted from the beginning of the decoded data.
     *
     * @param encryptedText the Base64 encoded encrypted string to be decrypted.
     * @return the decrypted plain text string.
     * @throws RuntimeException if an error occurs during decryption.
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, iv.length);

            byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] decryptedText = cipher.doFinal(encrypted);
            return new String(decryptedText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error during decryption: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Compares a plain text string with an encrypted text string by decrypting the latter
     * and performing a string equality check.
     *
     * @param plainText the unencrypted string to compare.
     * @param encryptedText the Base64 encoded encrypted string to compare against.
     * @return {@code true} if the decrypted text matches the plain text, {@code false} otherwise.
     *         Returns {@code false} if decryption fails.
     */
    public boolean matches(String plainText, String encryptedText) {
        try {
            String decrypted = decrypt(encryptedText);
            return plainText.equals(decrypted);
        } catch (Exception e) {
            log.warn("Error during comparison: {}", e.getMessage());
            return false;
        }
    }
}