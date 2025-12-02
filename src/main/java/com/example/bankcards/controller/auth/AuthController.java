package com.example.bankcards.controller.auth;

import com.example.bankcards.exception.user.DuplicateUsernameException;
import com.example.bankcards.model.dto.auth.AuthResponseDto;
import com.example.bankcards.model.dto.auth.LoginRequest;
import com.example.bankcards.model.dto.auth.RefreshRequest;
import com.example.bankcards.model.dto.auth.RegisterRequest;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration operations")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        int maxAgeSeconds = (int) (jwtUtil.getAccessTokenExpiration() / 1000);
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true для HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        setAccessTokenCookie(response, authResponse.getAccessToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "User registration")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        Optional<User> registeredUser = userRepository.findByUsername(registerRequest.getUsername());
        if(registeredUser.isPresent()) {
            throw new DuplicateUsernameException("Username " + registerRequest.getUsername()+ " already exists");
        }
        AuthResponseDto user = authService.register(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        setAccessTokenCookie(response, user.getAccessToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest, HttpServletResponse response) {
        String refreshToken = refreshRequest.getRefreshToken();
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        AuthResponseDto authResponse = authService.refreshAccessToken(refreshToken);
        setAccessTokenCookie(response, authResponse.getAccessToken());
        return ResponseEntity.ok(authResponse);
    }

    // НОВЫЙ МЕТОД ДЛЯ ВЫХОДА
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Удаляем куку
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully");
    }
}