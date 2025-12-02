package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_users")
public class User extends BasicEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Email
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    // НОВОЕ ПОЛЕ: Лимит трат (по умолчанию 5000)
    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit = new BigDecimal("5000.00");

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}