package com.example.bankcards.service.admin;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.dto.user.UserUpdateRequest;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing user-related operations specifically for administrators.
 * This service provides functionalities to retrieve, update, and delete user accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Retrieves a list of all registered users.
     *
     * @return A {@link List} of {@link UserResponseDto} representing all users.
     */
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves details of a specific user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return A {@link UserResponseDto} containing the user's details.
     * @throws UserNotFoundException if no user is found with the given ID.
     */
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return userMapper.toUserResponseDto(user);
    }

    /**
     * Updates details of an existing user by their ID.
     *
     * @param id The ID of the user to update.
     * @param request The request containing the updated user details (username, email, role).
     * @return A {@link UserResponseDto} containing the updated user's details.
     * @throws UserNotFoundException if no user is found with the given ID.
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        if (request.getUsername() != null) { user.setUsername(request.getUsername()); }
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        log.info("User with ID {} updated by admin.", id);
        return userMapper.toUserResponseDto(updatedUser);
    }

    /**
     * Deletes a user permanently by their ID.
     *
     * @param id The ID of the user to delete.
     * @throws UserNotFoundException if no user is found with the given ID.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("User with ID {} deleted by admin.", id);
    }
}