package com.shortUrlService.infrastructure.shortening;

import com.shortUrlService.config.AppConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ShortCodeGenerator {
    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String generateUniqueCode(String originalUrl, UUID userId) {
        try {
            String input = originalUrl + userId + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder code = new StringBuilder();
            for (int i = 0; i < AppConfig.getShortCodeLength(); i++) {
                int index = ((hash[i * 2] & 0xFF) + (hash[i * 2 + 1] & 0xFF))
                        % ALPHABET.length();
                code.append(ALPHABET.charAt(index));
            }
            return code.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
