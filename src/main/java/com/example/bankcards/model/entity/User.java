package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the bank card system.
 * This entity stores user authentication details, contact information, and their role.
 * It extends {@link BasicEntity} to inherit common fields like ID and timestamps.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_users")
public class User extends BasicEntity{

    /**
     * The unique username of the user.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * The unique email address of the user.
     * Validated as an email format.
     */
    @Email
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /**
     * The hashed password of the user.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The role of the user (e.g., ROLE_USER, ROLE_ADMIN).
     * Defaults to {@link Role#ROLE_USER}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    /**
     * A list of bank cards owned by this user.
     * One-to-many relationship, with cascade operations for all changes and fetched lazily.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cards = new ArrayList<>();

    /**
     * Constructor for creating a new user with essential details.
     * This constructor is useful for initial user creation without an ID or timestamps.
     *
     * @param username The username of the new user.
     * @param email The email address of the new user.
     * @param password The raw password of the new user (will be encoded before saving).
     * @param role The role assigned to the new user.
     */
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
