package com.example.bankcards.service;

import com.example.bankcards.exception.user.AuthenticationFailedException;
import com.example.bankcards.exception.user.DuplicateUsernameException;
import com.example.bankcards.model.dto.auth.AuthResponseDto;
import com.example.bankcards.model.entity.RefreshToken;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword", Role.ROLE_USER);
        testUser.setId(1L);
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponseDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshTokenString()).thenReturn("refreshTokenString");
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.login("testuser", "password");

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_withInvalidUsername_shouldThrowAuthenticationFailedException() {
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> {
            authService.login("wronguser", "password");
        });
        verify(userRepository).findByUsername("wronguser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_withInvalidPassword_shouldThrowAuthenticationFailedException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> {
            authService.login("testuser", "wrongpassword");
        });
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void register_withNewUsername_shouldReturnAuthResponseDto() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshTokenString()).thenReturn("refreshTokenString");
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.register("newuser", "new@example.com", "newpassword");

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository).findByUsername("newuser");
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_withExistingUsername_shouldThrowDuplicateUsernameException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(DuplicateUsernameException.class, () -> {
            authService.register("testuser", "test@example.com", "password");
        });
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshAccessToken_withValidToken_shouldReturnNewAuthResponseDto() {
        RefreshToken refreshToken = new RefreshToken(1L, "validToken", Instant.now().plusSeconds(3600), testUser);
        when(refreshTokenRepository.findByToken("validToken")).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshTokenString()).thenReturn("newRefreshTokenString");
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.refreshAccessToken("validToken");

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository).findByToken("validToken");
        verify(refreshTokenRepository).delete(refreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_withInvalidToken_shouldThrowRuntimeException() {
        when(refreshTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            authService.refreshAccessToken("invalidToken");
        });
    }

    @Test
    void refreshAccessToken_withExpiredToken_shouldThrowRuntimeException() {
        RefreshToken expiredToken = new RefreshToken(1L, "expiredToken", Instant.now().minusSeconds(3600), testUser);
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(expiredToken));

        assertThrows(RuntimeException.class, () -> {
            authService.refreshAccessToken("expiredToken");
        });
        verify(refreshTokenRepository).delete(expiredToken);
    }
}