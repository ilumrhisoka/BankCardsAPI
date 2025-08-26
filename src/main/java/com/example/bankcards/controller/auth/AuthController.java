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

/**
 * REST controller for user authentication operations.
 * This controller handles user login, registration, and token refreshing.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration operations")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * Handles user login. Authenticates the user with the provided credentials
     * and returns an access token and a refresh token upon successful authentication.
     *
     * @param loginRequest DTO containing username and password.
     * @return A {@link ResponseEntity} with {@link AuthResponseDto} on success (HTTP 200 OK)
     *         or an error message on failure (HTTP 401 Unauthorized).
     */
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
        return ResponseEntity.ok(response);
    }

    /**
     * Handles new user registration. Registers a new user with the provided details
     * and returns an access token and a refresh token upon successful registration.
     *
     * @param registerRequest DTO containing username, email, and password for registration.
     * @return A {@link ResponseEntity} with {@link AuthResponseDto} on success (HTTP 201 Created)
     *         or an error message on failure (HTTP 400 Bad Request if username exists or data is invalid).
     */
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
        Optional<User> registeredUser = userRepository.findByUsername(registerRequest.getUsername());
        if(registeredUser.isPresent()) {
            throw new DuplicateUsernameException("Username " + registerRequest.getUsername()+ " already exists");
        }
        AuthResponseDto user = authService.register(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);

    }

    /**
     * Refreshes the access token using a provided refresh token.
     * Issues a new access token and refresh token pair.
     *
     * @param refreshRequest DTO containing the refresh token string.
     * @return A {@link ResponseEntity} with {@link AuthResponseDto} containing new tokens on success (HTTP 200 OK)
     *         or an error message on failure (HTTP 400 Bad Request or 403 Forbidden).
     */
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
        AuthResponseDto authResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }

}