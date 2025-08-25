package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.username = :username OR t.toCard.user.username = :username ORDER BY t.createdAt DESC")
    List<Transfer> findByUserUsername(@Param("username") String username);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.createdAt DESC")
    List<Transfer> findByCardId(@Param("cardId") Long cardId);

}