package com.example.bankcards.repository;

import com.example.bankcards.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link com.example.bankcards.model.entity.RefreshToken} entities.
 * Provides methods for CRUD operations and custom queries related to refresh tokens.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string of the refresh token.
     * @return An {@link Optional} containing the {@link RefreshToken} if found, otherwise empty.
     */
    Optional<RefreshToken> findByToken(String token);
}
