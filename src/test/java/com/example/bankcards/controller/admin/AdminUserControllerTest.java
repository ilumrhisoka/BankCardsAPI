package com.example.bankcards.controller.admin;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.dto.user.UserUpdateRequest;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.admin.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {


    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponseDto testUserResponseDto;
    private UserUpdateRequest validUserUpdateRequest;

    @BeforeEach
    void setUp() {
        testUserResponseDto = new UserResponseDto();
        testUserResponseDto.setId(1L);
        testUserResponseDto.setUsername("testuser");
        testUserResponseDto.setEmail("test@example.com");
        testUserResponseDto.setRole(Role.ROLE_USER);

        validUserUpdateRequest = new UserUpdateRequest("updatedUser", "updated@example.com", Role.ROLE_ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnOkAndListOfUsers() throws Exception {
        List<UserResponseDto> users = Collections.singletonList(testUserResponseDto);
        given(adminUserService.getAllUsers()).willReturn(users);

        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(testUserResponseDto.getUsername()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnOkAndUser() throws Exception {
        given(adminUserService.getUserById(anyLong())).willReturn(testUserResponseDto);

        mockMvc.perform(get("/api/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserResponseDto.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_whenUserNotFound_shouldReturnNotFound() throws Exception {
        given(adminUserService.getUserById(anyLong())).willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/admin/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldReturnOkAndUpdatedUser() throws Exception {
        UserResponseDto updatedDto = new UserResponseDto();
        updatedDto.setId(1L);
        updatedDto.setUsername(validUserUpdateRequest.getUsername());
        updatedDto.setEmail(validUserUpdateRequest.getEmail());
        updatedDto.setRole(validUserUpdateRequest.getRole());

        given(adminUserService.updateUser(anyLong(), any(UserUpdateRequest.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedDto.getUsername()))
                .andExpect(jsonPath("$.email").value(updatedDto.getEmail()))
                .andExpect(jsonPath("$.role").value(updatedDto.getRole().name()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        UserUpdateRequest invalidRequest = new UserUpdateRequest("", "invalid-email", null); // Invalid data
        mockMvc.perform(put("/api/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        given(adminUserService.updateUser(anyLong(), any(UserUpdateRequest.class)))
                .willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/api/admin/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(adminUserService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/admin/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenUserNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(adminUserService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/admin/users/{id}", 99L)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}