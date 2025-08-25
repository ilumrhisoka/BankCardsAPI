package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank card in the system.
 * This entity stores details about a card, its status, balance, and its association with a user.
 * It extends {@link BasicEntity} to inherit common fields like ID and timestamps.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_cards")
public class Card extends BasicEntity {

    /**
     * The unique (and typically encrypted/hashed) card number.
     */
    @Column(nullable = false, length = 100)
    private String cardNumber;

    /**
     * The name of the card holder as it appears on the card.
     */
    @Column(nullable = false, length = 100)
    private String cardHolder;

    /**
     * The expiry date of the card.
     */
    @Column(nullable = false)
    private LocalDate expiryDate;

    /**
     * The current status of the card (e.g., ACTIVE, BLOCKED, BLOCKED_PENDING).
     * Defaults to {@link CardStatus#ACTIVE}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus = CardStatus.ACTIVE;

    /**
     * The current balance on the card.
     * Stored with a precision of 15 digits and 2 decimal places.
     * Defaults to {@link BigDecimal#ZERO}.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * The user who owns this card.
     * Many-to-one relationship, fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    /**
     * A list of transfers where this card is the recipient.
     * One-to-many relationship, fetched lazily.
     */
    @OneToMany(mappedBy="toCard", fetch = FetchType.LAZY)
    private List<Transfer> incomingTransfers = new ArrayList<>();

    /**
     * A list of transfers where this card is the sender.
     * One-to-many relationship, fetched lazily.
     */
    @OneToMany(mappedBy="fromCard", fetch = FetchType.LAZY)
    private List<Transfer> outgoingTransfers = new ArrayList<>();
}
