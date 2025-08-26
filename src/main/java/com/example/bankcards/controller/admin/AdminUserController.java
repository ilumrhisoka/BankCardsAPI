package com.example.bankcards.controller.admin;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.dto.user.UserUpdateRequest;
import com.example.bankcards.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.bankcards.exception.card.CardOwnershipException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.example.bankcards.exception.dto.ErrorResponse;
import java.util.List;

/**
 * REST controller for managing users by administrators.
 * This controller provides endpoints for retrieving, updating, and deleting user accounts.
 * All operations require the authenticated user to have the 'ROLE_ADMIN' authority.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "Operations related to managing users by administrators")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    private final AdminUserService userService;

    /**
     * Retrieves a list of all registered users.
     * This operation is accessible only by administrators.
     *
     * @return A {@link ResponseEntity} containing a list of user details
     *         ({@link UserResponseDto}) and HTTP status 200 (OK).
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Get all users",
            description = "Retrieves a list of all registered users. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves details of a specific user by their ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the user to retrieve.
     * @return A {@link ResponseEntity} containing the user details
     *         ({@link UserResponseDto}) and HTTP status 200 (OK).
     * @throws UserNotFoundException (HTTP 404) if no user is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Get user by ID",
            description = "Retrieves details of a specific user by their ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundException.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Allows administrators to update details of an existing user by their ID.
     *
     * @param id The ID of the user to update.
     * @param request The request body containing updated user details.
     * @return A {@link ResponseEntity} containing the updated user details
     *         ({@link UserResponseDto}) and HTTP status 200 (OK).
     * @throws UserNotFoundException (HTTP 404) if no user is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Update user details",
            description = "Allows administrators to update details of an existing user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Для общих ошибок валидации
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundException.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserUpdateRequest request) {
        UserResponseDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user permanently by their ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the user to delete.
     * @return A {@link ResponseEntity} with no content and HTTP status 204 (No Content)
     *         upon successful deletion.
     * @throws UserNotFoundException (HTTP 404) if no user is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Delete a user",
            description = "Deletes a user permanently by their ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "User not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundException.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}