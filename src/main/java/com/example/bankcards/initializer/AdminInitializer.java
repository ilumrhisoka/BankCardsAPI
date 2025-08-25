package com.example.bankcards.initializer;

import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Component that initializes a default administrator user if no admin user
 * exists in the database upon application startup.
 * This class implements {@link CommandLineRunner} to execute logic right after the
 * Spring application context has been loaded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configurable username for the default administrator.
     * Defaults to "admin" if not specified in application properties.
     */
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    /**
     * Configurable password for the default administrator.
     * Defaults to "password" if not specified in application properties.
     */
    @Value("${app.admin.password:password}")
    private String adminPassword;

    /**
     * Callback method executed once the Spring application context is fully loaded.
     * It checks if any user with the {@code ROLE_ADMIN} role exists in the database.
     * If no admin user is found, it creates a new admin user with the configured
     * username and password, and saves it to the database.
     *
     * @param args Command line arguments (not used in this implementation).
     * @throws Exception if an error occurs during user creation or database interaction.
     */
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(Role.ROLE_ADMIN)) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole(Role.ROLE_ADMIN);
            adminUser.setEmail(adminUsername + "@gmail.com");
            userRepository.save(adminUser);
            log.info("Admin user created: {}", adminUsername);
        } else {
            log.info("Admin user already exists: {}", adminUsername);
        }
    }
}
