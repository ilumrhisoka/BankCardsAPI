package com.example.bankcards.service.auth;

import com.example.bankcards.exception.user.AuthenticationFailedException;
import com.example.bankcards.exception.user.DuplicateUsernameException;
import com.example.bankcards.model.dto.auth.AuthResponseDto;
import com.example.bankcards.model.entity.RefreshToken;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Service class for handling user authentication, including login, registration, and token refreshing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates a user with the provided username and password.
     * If authentication is successful, generates and returns an access token and a refresh token.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return An {@link AuthResponseDto} containing the generated access token and refresh token.
     * @throws AuthenticationFailedException if the username or password is invalid.
     */
    @Transactional
    public AuthResponseDto login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
                RefreshToken refreshToken = createAndSaveRefreshToken(user);
                log.info("Login Success for user: {}", username);
                return new AuthResponseDto(token, refreshToken.getToken());
            }
        }
        log.warn("Authentication failed for user: {}", username);
        throw new AuthenticationFailedException("Invalid username or password");
    }

    /**
     * Registers a new user with the provided username, email, and password.
     * If registration is successful, generates and returns an access token and a refresh token for the new user.
     * The new user is assigned the 'ROLE_USER' role by default.
     *
     * @param username The desired username for the new user.
     * @param email The email address for the new user.
     * @param password The desired password for the new user.
     * @return An {@link AuthResponseDto} containing the generated access token and refresh token for the new user.
     * @throws DuplicateUsernameException if a user with the provided username already exists.
     */
    @Transactional
    public AuthResponseDto register(String username,String email, String password) {
        if(userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration failed: Username '{}' already exists", username);
            throw new DuplicateUsernameException("Username" + username + " already exists");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, email, encodedPassword, Role.valueOf("ROLE_USER"));
        userRepository.save(newUser);
        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole().name());
        RefreshToken refreshToken = createAndSaveRefreshToken(newUser);
        log.info("Register Success for user: {}", username);
        return new AuthResponseDto(token, refreshToken.getToken());
    }

    /**
     * Creates and saves a new refresh token for the given user.
     *
     * @param user The user for whom to create the refresh token.
     * @return The newly created and saved {@link RefreshToken} object.
     */
    private RefreshToken createAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshTokenString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()));
        log.info("Refresh Token created for user: {}", user.getUsername());
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Refreshes an access token using a provided refresh token.
     * Invalidates the old refresh token and issues a new one along with a new access token.
     *
     * @param refreshTokenString The string representation of the refresh token.
     * @return An {@link AuthResponseDto} containing the new access token and new refresh token.
     * @throws RuntimeException if the refresh token is not found, is invalid, or has expired,
     *                          or if the user associated with the refresh token is not found.
     */
    @Transactional
    public AuthResponseDto refreshAccessToken(String refreshTokenString) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByToken(refreshTokenString);
        if (optionalRefreshToken.isEmpty()) {
            throw new RuntimeException("Refresh Token не найден или недействителен.");
        }
        RefreshToken refreshToken = optionalRefreshToken.get();
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh Token истек. Пожалуйста, войдите снова.");
        }
        User user = refreshToken.getUser();
        if (user == null) {
            throw new RuntimeException("Пользователь, связанный с Refresh Token, не найден.");
        }
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        refreshTokenRepository.delete(refreshToken);
        RefreshToken newRefreshToken = createAndSaveRefreshToken(user);
        log.info("Access Token refreshed for user: {}", user.getUsername());

        return new AuthResponseDto(newAccessToken, newRefreshToken.getToken());
    }
}
