package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.auth.AuthResponseDto;
import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.dto.auth.RefreshRequest;
import com.example.bankcards.dto.auth.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;
import jakarta.persistence.GeneratedValue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        AuthResponseDto response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        AuthResponseDto token = authService.register(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        Optional<User> registeredUser = userRepository.findByUsername(registerRequest.getUsername());
        if (registeredUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(token);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists or invalid registration data.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            AuthResponseDto authResponse = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            System.err.println("Refresh token failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

}