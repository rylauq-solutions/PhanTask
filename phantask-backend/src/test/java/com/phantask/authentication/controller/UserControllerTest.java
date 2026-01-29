package com.phantask.authentication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import com.phantask.authentication.dto.AccountCreationResponse;
import com.phantask.authentication.dto.AdminEditUserRequest;
import com.phantask.authentication.dto.PasswordChangeRequest;
import com.phantask.authentication.dto.RegisterRequest;
import com.phantask.authentication.dto.UpdateProfileRequest;
import com.phantask.authentication.dto.UserProfileResponse;
import com.phantask.authentication.dto.UserResponse;
import com.phantask.authentication.service.api.IUserService;

/**
 * Integration tests for UserController
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    private RegisterRequest registerRequest;
    private AccountCreationResponse accountCreationResponse;
    private UserProfileResponse userProfileResponse;
    private List<UserResponse> activeUsers;
    private List<UserResponse> inactiveUsers;

    @BeforeEach
    void setUp() {
        reset(userService);

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setRole("USER");

        // Setup account creation response
        accountCreationResponse = new AccountCreationResponse(
            "newuser",
            "Account created successfully. Temporary password: TempPass123"
        );

        // Setup user profile response
        userProfileResponse = new UserProfileResponse(
            1L,
            "testuser",
            "test@example.com",
            "USER",
            true,
            false,
            LocalDateTime.now(),
            Set.of("USER"),
            "Test User",
            "IT",
            "1234567890",
            null,
            "2024",
            LocalDate.of(2000, 1, 1)
        );

        // Setup active users list
        UserResponse user1 = new UserResponse(1L, "user1", "user1@example.com", Arrays.asList("USER"), true, false, LocalDateTime.now(), null);
        UserResponse user2 = new UserResponse(2L, "user2", "user2@example.com", Arrays.asList("ADMIN"), true, false, LocalDateTime.now(), null);
        activeUsers = Arrays.asList(user1, user2);

        // Setup inactive users list
        UserResponse inactiveUser = new UserResponse(3L, "inactive", "inactive@example.com", Arrays.asList("USER"), false, false, LocalDateTime.now(), LocalDateTime.now());
        inactiveUsers = Arrays.asList(inactiveUser);
    }

    // ==================== POST /api/users/create-account Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.createAccount(anyString(), anyString())).thenReturn(accountCreationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/users/create-account")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.message").value("Account created successfully. Temporary password: TempPass123"));

        verify(userService).createAccount(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/users/create-account")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid email format"));

        verify(userService, never()).createAccount(anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_WithDuplicateEmail_ShouldReturn400() throws Exception {
        // Arrange
        when(userService.createAccount(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("User with this email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users/create-account")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User with this email already exists"));

        verify(userService).createAccount(anyString(), anyString());
    }

    @Test
    void createAccount_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/create-account")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden());

        verify(userService, never()).createAccount(anyString(), anyString());
    }

    // ==================== PUT /api/users/{userId}/edit Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void editUser_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        AdminEditUserRequest editRequest = new AdminEditUserRequest();
        editRequest.setFullName("Updated Name");
        editRequest.setDepartment("HR");
        editRequest.setPhone("9876543210");
        editRequest.setResetPassword(false);

        when(userService.editUserByAdmin(eq(1L), any(AdminEditUserRequest.class)))
                .thenReturn("User updated successfully");

        // Act & Assert
        mockMvc.perform(put("/api/users/1/edit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));

        verify(userService).editUserByAdmin(eq(1L), any(AdminEditUserRequest.class));
    }

    // ==================== PUT /api/users/{userId}/deactivate Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_WithValidUserId_ShouldReturn200() throws Exception {
        // Arrange
        doNothing().when(userService).deactivateUser(1L);

        // Act & Assert
        mockMvc.perform(put("/api/users/1/deactivate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account deactivated successfully"));

        verify(userService).deactivateUser(1L);
    }

    // ==================== PUT /api/users/{userId}/reactivate Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void reactivateUser_WithValidUserId_ShouldReturn200() throws Exception {
        // Arrange
        doNothing().when(userService).reactivateUser(3L);

        // Act & Assert
        mockMvc.perform(put("/api/users/3/reactivate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account reactivated successfully"));

        verify(userService).reactivateUser(3L);
    }

    // ==================== GET /api/users/active Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveUsers_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.getAllActiveUsers()).thenReturn(activeUsers);

        // Act & Assert
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getAllActiveUsers();
    }

    @Test
    void getActiveUsers_WithoutAuthentication_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllActiveUsers();
    }

    // ==================== GET /api/users/inactive Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInactiveUsers_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.getAllInactiveUsers()).thenReturn(inactiveUsers);

        // Act & Assert
        mockMvc.perform(get("/api/users/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("inactive"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getAllInactiveUsers();
    }

    // ==================== POST /api/users/change-password-first-login Tests ====================

    @Test
    void changePasswordFirstLogin_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest();
        passwordRequest.setUsername("testuser");
        passwordRequest.setOldPassword("TempPass123");
        passwordRequest.setNewPassword("NewPass456");

        when(userService.changePasswordFirstLogin(any(PasswordChangeRequest.class)))
                .thenReturn("Password changed successfully");

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password-first-login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(userService).changePasswordFirstLogin(any(PasswordChangeRequest.class));
    }

    // ==================== POST /api/users/update-profile-first-login Tests ====================

    @Test
    void updateProfileFirstLogin_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.updateProfileFirstLogin(eq("testuser"), any(UpdateProfileRequest.class)))
                .thenReturn("Profile updated successfully");

        // Act & Assert
        mockMvc.perform(post("/api/users/update-profile-first-login")
                .with(csrf())
                .param("username", "testuser")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("fullName", "Test User")
                .param("phone", "1234567890")
                .param("department", "IT"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));

        verify(userService).updateProfileFirstLogin(eq("testuser"), any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfileFirstLogin_WhenNotFirstLogin_ShouldReturn400() throws Exception {
        // Arrange
        when(userService.updateProfileFirstLogin(eq("testuser"), any(UpdateProfileRequest.class)))
                .thenThrow(new RuntimeException("Profile update not allowed - user has already completed first login"));

        // Act & Assert
        mockMvc.perform(post("/api/users/update-profile-first-login")
                .with(csrf())
                .param("username", "testuser")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("fullName", "Test User"))
                .andExpect(status().isForbidden());

        verify(userService).updateProfileFirstLogin(eq("testuser"), any(UpdateProfileRequest.class));
    }

    // ==================== GET /api/users/profile Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void getProfile_WithValidUser_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.getProfile("testuser")).thenReturn(userProfileResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(userService).getProfile("testuser");
    }

    // ==================== POST /api/users/update-profile Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.updateProfile(eq("testuser"), any(UpdateProfileRequest.class)))
                .thenReturn("Profile updated successfully");

        // Act & Assert
        mockMvc.perform(post("/api/users/update-profile")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("fullName", "Updated Name")
                .param("phone", "9876543210")
                .param("department", "HR"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));

        verify(userService).updateProfile(eq("testuser"), any(UpdateProfileRequest.class));
    }

    // ==================== POST /api/users/change-password Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void changePassword_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        PasswordChangeRequest passwordRequest = new PasswordChangeRequest();
        passwordRequest.setUsername("testuser");
        passwordRequest.setOldPassword("OldPass123");
        passwordRequest.setNewPassword("NewPass456");

        when(userService.changePassword(eq("testuser"), any(PasswordChangeRequest.class)))
                .thenReturn("Password changed successfully");

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(userService).changePassword(eq("testuser"), any(PasswordChangeRequest.class));
    }
}
