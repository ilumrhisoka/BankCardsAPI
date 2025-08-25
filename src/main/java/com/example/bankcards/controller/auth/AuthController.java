package com.example.bankcards.controller.auth;

import com.example.bankcards.exception.user.DuplicateUsernameException;
import com.example.bankcards.model.dto.auth.AuthResponseDto;
import com.example.bankcards.model.dto.auth.LoginRequest;
import com.example.bankcards.model.dto.auth.RefreshRequest;
import com.example.bankcards.model.dto.auth.RegisterRequest;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration operations")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "User login",
            description = "Authenticates a user and returns JWT tokens (access and refresh).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Invalid username or password.")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponseDto response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
    }

    @Operation(summary = "User registration",
            description = "Registers a new user and returns JWT tokens upon successful registration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Username already exists or invalid registration data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Username already exists or invalid registration data.")))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponseDto user = authService.register(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        Optional<User> registeredUser = userRepository.findByUsername(registerRequest.getUsername());
        if (registeredUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DuplicateUsernameException("Username already exists or invalid registration data"));
    }

    @Operation(summary = "Refresh access token",
            description = "Uses a refresh token to obtain a new access token and refresh token pair.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token is missing",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Refresh token is missing."))),
            @ApiResponse(responseCode = "403", description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class, example = "Invalid or expired refresh token.")))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
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