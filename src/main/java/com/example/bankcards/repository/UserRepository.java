package com.example.bankcards.repository;

import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for {@link com.example.bankcards.model.entity.User} entities.
 * Provides methods for CRUD operations and custom queries related to user accounts.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find.
     * @return An {@link Optional} containing the {@link User} if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if any user exists with a specific role.
     *
     * @param role The role to check for existence.
     * @return {@code true} if at least one user with the specified role exists, {@code false} otherwise.
     */
    boolean existsByRole(Role role);
}
