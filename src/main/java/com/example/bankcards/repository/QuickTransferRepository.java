package com.example.bankcards.repository;

import com.example.bankcards.model.entity.QuickTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuickTransferRepository extends JpaRepository<QuickTransfer, Long> {
    List<QuickTransfer> findByUserUsername(String username);
}