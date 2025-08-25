package com.example.bankcards.repository;

import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByRole(Role role);
}
