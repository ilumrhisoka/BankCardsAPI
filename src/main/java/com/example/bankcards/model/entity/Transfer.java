package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a money transfer transaction between two cards.
 * This entity stores details about the sender, receiver, amount, and status of a transfer.
 * It extends {@link BasicEntity} to inherit common fields like ID and timestamps.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_transfers")
public class Transfer extends BasicEntity{

    /**
     * The card from which the money is transferred.
     * Many-to-one relationship with the {@link Card} entity, fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    /**
     * The card to which the money is transferred.
     * Many-to-one relationship with the {@link Card} entity, fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    /**
     * The amount of money transferred.
     * Stored with a precision of 15 digits and 2 decimal places.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * The date and time when the transfer occurred.
     */
    @Column(nullable = false)
    private LocalDateTime transferDate;

    /**
     * The current status of the transfer (e.g., COMPLETED, FAILED, PENDING).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;
}