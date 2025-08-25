package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.user.username = :username")
    Page<Card> findByUserUsernamePageable(@Param("username") String username, Pageable pageable);

    List<Card> findByUserUsername(String username);
}
