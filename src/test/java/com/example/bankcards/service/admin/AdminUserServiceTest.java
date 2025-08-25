package com.example.bankcards.service.admin;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.dto.user.UserUpdateRequest;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserService adminUserService;

    private User testUser;
    private UserResponseDto testUserResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", Role.ROLE_USER);
        testUser.setId(1L);

        testUserResponseDto = new UserResponseDto();
        testUserResponseDto.setId(1L);
        testUserResponseDto.setUsername("testuser");
        testUserResponseDto.setEmail("test@example.com");
        testUserResponseDto.setRole(Role.ROLE_USER);
    }

    @Test
    void getAllUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

        List<UserResponseDto> result = adminUserService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository).findAll();
        verify(userMapper).toUserResponseDto(testUser);
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponseDto(testUser)).thenReturn(testUserResponseDto);

        UserResponseDto result = adminUserService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponseDto(testUser);
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            adminUserService.getUserById(99L);
        });
        verify(userRepository).findById(99L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateAndReturnUser() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("updatedUser", "updated@example.com", Role.ROLE_ADMIN);
        User updatedTestUser = new User("updatedUser", "updated@example.com", "password", Role.ROLE_ADMIN);
        updatedTestUser.setId(1L);
        UserResponseDto updatedUserResponseDto = new UserResponseDto();
        updatedUserResponseDto.setId(1L);
        updatedUserResponseDto.setUsername("updatedUser");
        updatedUserResponseDto.setEmail("updated@example.com");
        updatedUserResponseDto.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedTestUser);
        when(userMapper.toUserResponseDto(updatedTestUser)).thenReturn(updatedUserResponseDto);

        UserResponseDto result = adminUserService.updateUser(1L, updateRequest);

        assertNotNull(result);
        assertEquals("updatedUser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(Role.ROLE_ADMIN, result.getRole());

        assertEquals("updatedUser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals(Role.ROLE_ADMIN, testUser.getRole());

        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userMapper).toUserResponseDto(updatedTestUser);
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            adminUserService.updateUser(99L, updateRequest);
        });
        verify(userRepository).findById(99L);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_shouldCallDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        adminUserService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            adminUserService.deleteUser(99L);
        });
        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}