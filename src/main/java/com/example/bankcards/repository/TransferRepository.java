package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    @Query("SELECT t FROM Transfer t WHERE t.fromCard.user.username = :username OR t.toCard.user.username = :username ORDER BY t.createdAt DESC")
    List<Transfer> findByUserUsername(@Param("username") String username);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.createdAt DESC")
    List<Transfer> findByCardId(@Param("cardId") Long cardId);

    @Query("SELECT t FROM Transfer t WHERE t.status = :status")
    List<Transfer> findByStatus(@Param("status") TransferStatus status);

    @Query("SELECT t FROM Transfer t WHERE t.transferDate BETWEEN :startDate AND :endDate")
    List<Transfer> findByTransferDateBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId ORDER BY t.createdAt DESC")
    List<Transfer> findOutgoingTransfersByCardId(@Param("cardId") Long cardId);

    @Query("SELECT t FROM Transfer t WHERE t.toCard.id = :cardId ORDER BY t.createdAt DESC")
    List<Transfer> findIncomingTransfersByCardId(@Param("cardId") Long cardId);
}