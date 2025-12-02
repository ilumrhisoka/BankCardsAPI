package com.example.bankcards.repository;

import com.example.bankcards.model.entity.ServiceFee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceFeeRepository extends JpaRepository<ServiceFee, Long> {
    List<ServiceFee> findByAccount_User_Username(String username);
    List<ServiceFee> findByAccount_IdAndIsPaidFalse(Long accountId);
}