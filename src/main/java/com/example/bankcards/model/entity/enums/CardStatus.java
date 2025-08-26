package com.example.bankcards.model.entity.enums;

/**
 * Enumeration representing the possible statuses of a bank card.
 */
public enum CardStatus {

    /**
     * The card is active and can be used for transactions.
     */
    ACTIVE,

    /**
     * The card is blocked and cannot be used.
     */
    BLOCKED,

    /**
     * The card has expired and is no longer valid.
     */
    EXPIRED,

    /**
     * The card is pending a block operation.
     */
    PENDING_BLOCK,

    /**
     * The card is pending an unblock operation.
     */
    PENDING_UNBLOCK
}
