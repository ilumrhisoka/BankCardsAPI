package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for {@link com.example.bankcards.model.entity.Card} entities.
 * Provides methods for CRUD operations and custom queries related to bank cards.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * Finds a paginated list of cards associated with a specific username.
     *
     * @param username The username of the user whose cards are to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link Card} entities belonging to the specified user.
     */
    @Query("SELECT c FROM Card c WHERE c.user.username = :username")
    Page<Card> findByUserUsernamePageable(@Param("username") String username, Pageable pageable);

    /**
     * Finds all cards associated with a specific username.
     *
     * @param username The username of the user whose cards are to be retrieved.
     * @return A {@link List} of {@link Card} entities belonging to the specified user.
     */
    List<Card> findByUserUsername(String username);
}
