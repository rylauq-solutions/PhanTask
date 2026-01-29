package com.phantask.authentication.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantask.authentication.dto.LoginRequest;
import com.phantask.authentication.service.api.IAuthService;
import com.phantask.exception.AccountDeactivatedException;

/**
 * Integration tests for AuthController
 * 
 * Tests only existing endpoints:
 * - POST /api/auth/login
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAuthService authService;

    private LoginRequest loginRequest;
    private Map<String, Object> loginResponse;

    @BeforeEach
    void setUp() {
        reset(authService);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Setup successful login response
        loginResponse = new HashMap<>();
        loginResponse.put("token", "access-token-123");
        loginResponse.put("refreshToken", "refresh-token-456");
        loginResponse.put("requirePasswordChange", false);
        loginResponse.put("role", Set.of("USER"));
    }

    // ==================== POST /api/auth/login Tests ====================

    @Test
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.requirePasswordChange").value(false));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithDeactivatedAccount_ShouldReturn403() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountDeactivatedException("Account is deactivated. Please contact admin."));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is deactivated. Please contact admin."));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithFirstLoginUser_ShouldReturnPasswordChangeRequired() throws Exception {
        // Arrange
        Map<String, Object> firstLoginResponse = new HashMap<>();
        firstLoginResponse.put("requirePasswordChange", true);
        firstLoginResponse.put("message", "Password change required before login");

        when(authService.login(any(LoginRequest.class))).thenReturn(firstLoginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requirePasswordChange").value(true))
                .andExpect(jsonPath("$.message").value("Password change required before login"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithEmptyUsername_ShouldReturn400() throws Exception {
        // Arrange
        loginRequest.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithEmptyPassword_ShouldReturn400() throws Exception {
        // Arrange
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithNullCredentials_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidRequestFormat_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}")) // Missing password field
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }


    @Test
    void login_WithMixedCaseUsername_ShouldReturn200() throws Exception {
        // Arrange
        loginRequest.setUsername("TestUser");
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithLongPassword_ShouldReturn200() throws Exception {
        // Arrange
        loginRequest.setPassword("VeryLongPasswordWith123NumbersAndSpecialChars!@#");
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        verify(authService).login(any(LoginRequest.class));
    }
}
