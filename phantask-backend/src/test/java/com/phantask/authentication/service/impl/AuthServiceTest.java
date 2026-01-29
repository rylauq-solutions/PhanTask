package com.phantask.authentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.phantask.authentication.dto.LoginRequest;
import com.phantask.authentication.entity.Role;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.authentication.security.JwtUtil;
import com.phantask.exception.AccountDeactivatedException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test user with roles
        testUser = new User();
        testUser.setUid(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEnabled(true);
        testUser.setFirstLogin(false);
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup roles
        Role userRole = new Role();
        userRole.setRid(1L);
        userRole.setRoleName("USER");

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Setup authentication object
        authentication = new UsernamePasswordAuthenticationToken(
            testUser,
            null,
            testUser.getAuthorities()
        );
    }

    // ==================== login() Tests ====================

    @Test
    void login_WithValidCredentials_ShouldReturnTokensAndUserInfo() {
        // Arrange
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(testUser))
                .thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(testUser))
                .thenReturn("refresh-token-456");

        // Act
        Map response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token-123", response.get("token"));
        assertEquals("refresh-token-456", response.get("refreshToken"));
        assertEquals(false, response.get("requirePasswordChange"));
        
        // Check roles (returns as "role", not "roles")
        assertNotNull(response.get("role"));
        assertTrue(response.get("role") instanceof List);
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) response.get("role");
        assertTrue(roles.contains("USER"));

        verify(userRepo).findByUsername("testuser");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUsername("wronguser"))
                .thenReturn(Optional.empty());

        loginRequest.setUsername("wronguser");

        // Act & Assert
        BadCredentialsException exception = assertThrows(
            BadCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepo).findByUsername("wronguser");
        verify(authManager, never()).authenticate(any());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));

        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(userRepo).findByUsername("testuser");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void login_WithDisabledAccount_ShouldThrowException() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        AccountDeactivatedException exception = assertThrows(
            AccountDeactivatedException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Account is deactivated. Please contact admin.", exception.getMessage());
        verify(userRepo).findByUsername("testuser");
        verify(authManager, never()).authenticate(any());
    }

    @Test
    void login_WithFirstLoginUser_ShouldReturnPasswordChangeRequired() {
        // Arrange
        testUser.setFirstLogin(true);
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act
        Map response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(true, response.get("requirePasswordChange"));
        assertEquals("Password change required before login", response.get("message"));
        assertFalse(response.containsKey("token"));
        assertFalse(response.containsKey("refreshToken"));

        verify(jwtUtil, never()).generateAccessToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void login_WithMultipleRoles_ShouldReturnAllRoles() {
        // Arrange
        Role adminRole = new Role();
        adminRole.setRid(2L);
        adminRole.setRoleName("ADMIN");
        testUser.getRoles().add(adminRole);

        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(testUser))
                .thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(testUser))
                .thenReturn("refresh-token-456");

        // Act
        Map response = authService.login(loginRequest);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) response.get("role");
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    void login_ShouldNotExposePasswordInResponse() {
        // Arrange
        when(userRepo.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(testUser))
                .thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(testUser))
                .thenReturn("refresh-token-456");

        // Act
        Map response = authService.login(loginRequest);

        // Assert
        assertFalse(response.containsKey("password"));
    }

    // ==================== refreshToken() Tests ====================

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(jwtUtil.isTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("new-access-token-789");

        // Act
        String newAccessToken = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(newAccessToken);
        assertEquals("new-access-token-789", newAccessToken);
        
        verify(jwtUtil).extractUsername(refreshToken);
        verify(jwtUtil).isRefreshToken(refreshToken);
        verify(userRepo).findByUsername(username);
        verify(jwtUtil).isTokenValid(refreshToken, testUser);
        verify(jwtUtil).generateAccessToken(testUser);
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowException() {
        // Arrange
        String expiredToken = "expired-refresh-token";
        
        when(jwtUtil.extractUsername(expiredToken))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> authService.refreshToken(expiredToken));
        
        verify(jwtUtil).extractUsername(expiredToken);
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_WithAccessTokenInsteadOfRefreshToken_ShouldThrowException() {
        // Arrange
        String accessToken = "access-token-not-refresh";
        
        when(jwtUtil.extractUsername(accessToken)).thenReturn("testuser");
        when(jwtUtil.isRefreshToken(accessToken)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.refreshToken(accessToken)
        );

        assertEquals("Invalid token type. Only refresh tokens are allowed.", exception.getMessage());
        verify(jwtUtil).extractUsername(accessToken);
        verify(jwtUtil).isRefreshToken(accessToken);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid-token";
        
        when(jwtUtil.extractUsername(invalidToken))
                .thenThrow(new JwtException("Invalid token signature"));

        // Act & Assert
        assertThrows(JwtException.class, () -> authService.refreshToken(invalidToken));
        
        verify(jwtUtil).extractUsername(invalidToken);
    }

    @Test
    void refreshToken_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String username = "nonexistent";
        
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.refreshToken(refreshToken)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_WhenTokenValidationFails_ShouldThrowException() {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        String username = "testuser";
        
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(jwtUtil.isTokenValid(refreshToken, testUser)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.refreshToken(refreshToken)
        );

        assertEquals("Refresh token expired. Please login again.", exception.getMessage());
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    // ==================== getCurrentUserProfile() Tests ====================

    @Test
    void getCurrentUserProfile_WithValidToken_ShouldReturnUserProfile() {
        // Arrange
        String token = "valid-access-token";
        String username = "testuser";
        
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        Map profile = authService.getCurrentUserProfile(token);

        // Assert
        assertNotNull(profile);
        assertEquals("testuser", profile.get("username"));
        assertEquals(true, profile.get("enabled"));
        assertEquals(false, profile.get("firstLogin"));
        assertNotNull(profile.get("roles"));

        verify(jwtUtil).extractUsername(token);
        verify(userRepo).findByUsername(username);
    }

    @Test
    void getCurrentUserProfile_WithExpiredToken_ShouldThrowException() {
        // Arrange
        String expiredToken = "expired-token";
        
        when(jwtUtil.extractUsername(expiredToken))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act & Assert
        assertThrows(ExpiredJwtException.class, 
            () -> authService.getCurrentUserProfile(expiredToken));

        verify(jwtUtil).extractUsername(expiredToken);
        verify(userRepo, never()).findByUsername(any());
    }

    @Test
    void getCurrentUserProfile_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid-token";
        
        when(jwtUtil.extractUsername(invalidToken))
                .thenThrow(new JwtException("Invalid token"));

        // Act & Assert
        assertThrows(JwtException.class, 
            () -> authService.getCurrentUserProfile(invalidToken));
    }

    @Test
    void getCurrentUserProfile_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        String token = "valid-token";
        String username = "nonexistent";
        
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> authService.getCurrentUserProfile(token)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getCurrentUserProfile_ShouldIncludeAllRoles() {
        // Arrange
        String token = "valid-token";
        String username = "testuser";
        
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");
        testUser.getRoles().add(adminRole);
        
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        Map profile = authService.getCurrentUserProfile(token);

        // Assert
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) profile.get("roles");
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }
}
