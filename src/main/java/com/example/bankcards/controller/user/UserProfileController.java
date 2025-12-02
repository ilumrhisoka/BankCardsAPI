package com.example.bankcards.controller.user;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Profile management and settings")
public class UserProfileController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Operation(summary = "Get current user profile")
    @GetMapping
    public ResponseEntity<UserResponseDto> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(userMapper.toUserResponseDto(user));
    }

    @Operation(summary = "Update monthly spending limit")
    @PostMapping("/limit")
    @Transactional
    public ResponseEntity<UserResponseDto> updateLimit(@RequestBody LimitUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }

        user.setMonthlyLimit(request.getLimit());
        userRepository.save(user);

        return ResponseEntity.ok(userMapper.toUserResponseDto(user));
    }

    @Data
    public static class LimitUpdateRequest {
        private BigDecimal limit;
    }
}