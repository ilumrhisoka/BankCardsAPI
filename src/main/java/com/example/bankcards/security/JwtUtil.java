package com.example.bankcards.security;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter; // Убедитесь, что Lombok импортирован
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Utility class for JSON Web Token (JWT) operations.
 * Handles generation, validation, and parsing of JWTs for authentication and authorization.
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    // Добавляем @Getter, чтобы получить доступ к времени истечения
    @Getter
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Getter
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;
    private SecretKey key;

    /**
     * Initializes the {@link Key} object from the secret string after the bean has been constructed.
     * This method is called automatically by Spring after dependency injection is complete.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a new JWT access token for a given username and role.
     * The token includes the subject (username), a custom claim for role,
     * issue date, and expiration date.
     *
     * @param username the subject of the token (e.g., user's identifier).
     * @param role the role of the user, included as a custom claim.
     * @return a compact JWT string.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Validates a given JWT token.
     * It parses the token using the signing key and checks its integrity and expiration.
     *
     * @param token the JWT string to validate.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // В реальном приложении здесь стоит логировать ошибку (например, ExpiredJwtException)
            return false;
        }
    }

    /**
     * Extracts the username (subject) from a valid JWT token.
     *
     * @param token the JWT string from which to extract the username.
     * @return the username (subject) contained within the token.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extracts the role from a valid JWT token's claims.
     * The role is expected to be stored under the "role" claim.
     *
     * @param token the JWT string from which to extract the role.
     * @return the role string contained within the token's claims.
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    /**
     * Generates a random UUID string to be used as a refresh token.
     *
     * @return a unique string representing a refresh token.
     */
    public String generateRefreshTokenString() {
        return UUID.randomUUID().toString();
    }
}