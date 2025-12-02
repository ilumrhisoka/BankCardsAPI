package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Найти все транзакции по ID счета
    List<Transaction> findByAccountId(Long accountId);

    // Найти все транзакции по имени пользователя (через связь Account -> User)
    Page<Transaction> findByAccount_User_Username(String username, Pageable pageable);
}