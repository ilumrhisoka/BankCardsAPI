package com.example.bankcards.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a refresh token used for obtaining new access tokens without re-authentication.
 * Each refresh token is associated with a specific user and has an expiry date.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    /**
     * Unique identifier for the refresh token.
     * Auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The refresh token string itself.
     * Must be unique.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * The expiration timestamp of the refresh token.
     */
    @Column(nullable = false)
    private Instant expiryDate;

    /**
     * The user associated with this refresh token.
     * One-to-one relationship with the {@link User} entity.
     */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    /**
     * Returns the token string when this object is converted to a string.
     *
     * @return The refresh token string.
     */
    @Override
    public String toString() {
        return token;
    }
}
