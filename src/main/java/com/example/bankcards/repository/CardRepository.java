package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.user.username = :username")
    List<Card> findByUserUsername(@Param("username") String username);

    Optional<Card> findByCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c WHERE c.cardStatus = :status")
    Page<Card> findByCardStatus(@Param("status") CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.username = :username AND c.cardStatus = 'ACTIVE'")
    Long countActiveCardsByUsername(@Param("username") String username);
}
