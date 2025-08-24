package com.example.bankcards.service;

import com.example.bankcards.dto.auth.AuthResponseDto;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDto login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(passwordEncoder.matches(password, user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
                RefreshToken refreshToken = createAndSaveRefreshToken(user);
                refreshTokenRepository.save(refreshToken);
                log.info("Login Success for user: {}", username);
                return new AuthResponseDto(token, refreshToken.getToken());
            }
        }
        log.info("Username not found");
        return null;
    }

    @Transactional
    public AuthResponseDto register(String username,String email, String password) {
        if(userRepository.findByUsername(username).isPresent()) {
            log.info("Username already exists");
            return null;
        }
        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, email, encodedPassword, Role.valueOf("ROLE_USER"));
        userRepository.save(newUser);
        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole().name());
        RefreshToken refreshToken = createAndSaveRefreshToken(newUser);
        log.info("Register Success for user: {}", username);
        return new AuthResponseDto(token, refreshToken.getToken());
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshTokenString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()));
        log.info("Refresh Token created for user: {}", user.getUsername());
        return refreshTokenRepository.save(refreshToken);
    }

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
