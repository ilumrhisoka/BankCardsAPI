package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for {@link com.example.bankcards.model.entity.Transfer} entities.
 * Provides methods for CRUD operations and custom queries related to money transfers.
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    /**
     * Finds all transfers where the given username is either the sender or receiver.
     * Transfers are ordered by creation date in descending order.
     *
     * @param username The username of the user to find transfers for.
     * @return A {@link List} of {@link Transfer} entities associated with the specified user.
     */
    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.username = :username OR t.toCard.user.username = :username ORDER BY t.createdAt DESC")
    List<Transfer> findByUserUsername(@Param("username") String username);

    /**
     * Finds all transfers associated with a specific card ID, either as a source or destination card.
     * Transfers are ordered by creation date in descending order.
     *
     * @param cardId The ID of the card to find transfers for.
     * @return A {@link List} of {@link Transfer} entities associated with the specified card.
     */
    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.createdAt DESC")
    List<Transfer> findByCardId(@Param("cardId") Long cardId);

}