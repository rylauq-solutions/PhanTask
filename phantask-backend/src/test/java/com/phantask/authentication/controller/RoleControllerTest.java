package com.phantask.authentication.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantask.authentication.service.api.IRoleService;

/**
 * Integration tests for RoleController
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IRoleService roleService;

    @BeforeEach
    void setUp() {
        reset(roleService);
    }

    // ==================== POST /api/roles/add Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_WithAdminRole_ShouldReturn201() throws Exception {
        // Arrange
        String roleName = "EDITOR";
        Map<String, String> request = Map.of("roleName", roleName);
        doNothing().when(roleService).addRole(roleName);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Role 'EDITOR' added successfully"));

        verify(roleService).addRole(roleName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_WithLowercaseRoleName_ShouldAccept() throws Exception {
        // Arrange
        String roleName = "editor";
        Map<String, String> request = Map.of("roleName", roleName);
        doNothing().when(roleService).addRole(roleName);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Role 'EDITOR' added successfully"));

        verify(roleService).addRole(roleName);
    }

    @Test
    @WithMockUser(roles = "USER")
    void addRole_WithUserRole_ShouldReturn403() throws Exception {
        // Arrange
        Map<String, String> request = Map.of("roleName", "EDITOR");

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roleService, never()).addRole(anyString());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void addRole_WithManagerRole_ShouldReturn403() throws Exception {
        // Arrange
        Map<String, String> request = Map.of("roleName", "EDITOR");

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roleService, never()).addRole(anyString());
    }

    @Test
    void addRole_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Arrange
        Map<String, String> request = Map.of("roleName", "EDITOR");

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(roleService, never()).addRole(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_WithDuplicateRoleName_ShouldReturn400() throws Exception {
        // Arrange
        String roleName = "ADMIN";
        Map<String, String> request = Map.of("roleName", roleName);
        doThrow(new IllegalArgumentException("Role 'ADMIN' already exists"))
                .when(roleService).addRole(roleName);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Role 'ADMIN' already exists"));

        verify(roleService).addRole(roleName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_WithServiceException_ShouldReturn500() throws Exception {
        // Arrange
        String roleName = "EDITOR";
        Map<String, String> request = Map.of("roleName", roleName);
        doThrow(new RuntimeException("Database connection failed"))
                .when(roleService).addRole(roleName);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to add role"));

        verify(roleService).addRole(roleName);
    }

    // ==================== GET /api/roles/all Tests ====================

    @Test
    @WithMockUser
    void getAllRoles_WithAuthenticatedUser_ShouldReturn200() throws Exception {
        // Arrange
        List<String> roles = Arrays.asList("ADMIN", "USER", "MANAGER", "HR");
        when(roleService.getAllRoles()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("USER"))
                .andExpect(jsonPath("$[2]").value("MANAGER"))
                .andExpect(jsonPath("$[3]").value("HR"));

        verify(roleService).getAllRoles();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllRoles_WithUserRole_ShouldReturn200() throws Exception {
        // Arrange
        List<String> roles = Arrays.asList("USER", "ADMIN");
        when(roleService.getAllRoles()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(roleService).getAllRoles();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRoles_WithAdminRole_ShouldReturn200() throws Exception {
        // Arrange
        List<String> roles = Arrays.asList("ADMIN", "USER");
        when(roleService.getAllRoles()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(roleService).getAllRoles();
    }

    @Test
    void getAllRoles_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isForbidden());

        verify(roleService, never()).getAllRoles();
    }

    @Test
    @WithMockUser
    void getAllRoles_WithNoRoles_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(roleService.getAllRoles()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(roleService).getAllRoles();
    }

    @Test
    @WithMockUser
    void getAllRoles_WithServiceException_ShouldReturn500() throws Exception {
        // Arrange
        when(roleService.getAllRoles())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/roles/all"))
                .andExpect(status().isInternalServerError());

        verify(roleService).getAllRoles();
    }

    // ==================== Edge Cases ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_WithSpecialCharacters_ShouldAccept() throws Exception {
        // Arrange
        String roleName = "SUPER_ADMIN";
        Map<String, String> request = Map.of("roleName", roleName);
        doNothing().when(roleService).addRole(roleName);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Role 'SUPER_ADMIN' added successfully"));

        verify(roleService).addRole(roleName);
    }
}
