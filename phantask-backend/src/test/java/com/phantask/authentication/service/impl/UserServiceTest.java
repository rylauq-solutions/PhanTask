package com.phantask.authentication.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.phantask.authentication.dto.AccountCreationResponse;
import com.phantask.authentication.dto.PasswordChangeRequest;
import com.phantask.authentication.dto.UpdateProfileRequest;
import com.phantask.authentication.dto.UserProfileResponse;
import com.phantask.authentication.entity.Role;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.entity.UserProfile;
import com.phantask.authentication.repository.RoleRepository;
import com.phantask.authentication.repository.UserProfileRepository;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.notification.email.EmailService;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private UserProfileRepository profileRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserProfile testProfile;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
	void setUp() {
	    // Setup roles
	    userRole = new Role();
	    userRole.setRid(1L);
	    userRole.setRoleName("USER");
	
	    adminRole = new Role();
	    adminRole.setRid(2L);
	    adminRole.setRoleName("ADMIN");
	
	    // Setup test user
	    testUser = new User();
	    testUser.setUid(1L);
	    testUser.setUsername("testuser");
	    testUser.setEmail("test@example.com");
	    testUser.setPassword("$2a$10$encodedPassword");
	    testUser.setEnabled(true);
	    testUser.setFirstLogin(false);
	    testUser.setCreatedAt(LocalDateTime.now());
	
	    Set<Role> roles = new HashSet<>();
	    roles.add(userRole);
	    testUser.setRoles(roles);
	
	    // Setup test profile (without setting profileId - it's auto-generated)
	    testProfile = new UserProfile();
	    testProfile.setUser(testUser);
	    testProfile.setFullName("Test User");
	    testProfile.setPhone("1234567890");
	    testProfile.setDepartment("IT");
	    testProfile.setYearOfStudy("2024");
	    testProfile.setDob(LocalDate.of(2000, 1, 1));
	    
	    testUser.setProfile(testProfile);
	}


    // ==================== loadUserByUsername() Tests ====================

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Arrange
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userRepo).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, 
            () -> userService.loadUserByUsername("nonexistent"));

        verify(userRepo).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_WithMultipleRoles_ShouldReturnAllAuthorities() {
        // Arrange
        testUser.getRoles().add(adminRole);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    // ==================== createAccount() Tests ====================

    @Test
    void createAccount_WithValidData_ShouldCreateUserAndSendEmail() {
        // Arrange
        String email = "newuser@example.com";
        String roleName = "USER";
        
        List<Role> allRoles = Arrays.asList(userRole, adminRole);
        when(roleRepo.findAll()).thenReturn(allRoles);
        when(roleRepo.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedTempPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUid(2L);
            return user;
        });
        doNothing().when(emailService).sendAccountCreationEmail(anyString(), anyString(), anyString());

        // Act
        AccountCreationResponse response = userService.createAccount(email, roleName);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getUsername());
        assertTrue(response.getUsername().startsWith("newuser"));

        verify(roleRepo).findAll();
        verify(roleRepo).findByRoleName("USER");
        verify(passwordEncoder).encode("Temp@123");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void createAccount_WithInvalidRole_ShouldThrowException() {
        // Arrange
        String email = "newuser@example.com";
        String roleName = "INVALID_ROLE";
        
        List<Role> allRoles = Arrays.asList(userRole, adminRole);
        when(roleRepo.findAll()).thenReturn(allRoles);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createAccount(email, roleName)
        );

        assertTrue(exception.getMessage().contains("Invalid role"));
        verify(roleRepo).findAll();
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void createAccount_WithDuplicateEmail_ShouldIncrementUsername() {
        // Arrange
        String email = "test@example.com";
        String roleName = "USER";
        
        List<Role> allRoles = Arrays.asList(userRole);
        when(roleRepo.findAll()).thenReturn(allRoles);
        when(roleRepo.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(userRepo.existsByUsername("test")).thenReturn(true);
        when(userRepo.existsByUsername("test1")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AccountCreationResponse response = userService.createAccount(email, roleName);

        // Assert
        assertNotNull(response);
        assertEquals("test1", response.getUsername());
    }

    // ==================== changePassword() Tests ====================

    /*
     * @Test
	 * void changePassword_WithValidOldPassword_ShouldUpdatePassword() { // Arrange
	 * PasswordChangeRequest request = new PasswordChangeRequest();
	 * request.setUsername("testuser"); request.setOldPassword("oldPassword123");
	 * request.setNewPassword("NewPassword123");
	 * 
	 * when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
	 * when(passwordEncoder.matches("oldPassword123",
	 * testUser.getPassword())).thenReturn(true);
	 * when(passwordEncoder.matches("NewPassword123",
	 * testUser.getPassword())).thenReturn(false);
	 * when(passwordEncoder.encode("NewPassword123")).thenReturn(
	 * "$2a$10$newEncodedPassword");
	 * when(userRepo.save(any(User.class))).thenReturn(testUser);
	 * 
	 * // Act String result = userService.changePassword("testuser", request);
	 * 
	 * // Assert assertEquals("Password changed successfully", result);
	 * assertFalse(testUser.isFirstLogin());
	 * 
	 * // Verify the correct interactions
	 * verify(userRepo).findByUsername("testuser");
	 * verify(passwordEncoder).matches("oldPassword123", testUser.getPassword()); //
	 * Check old password verify(passwordEncoder).matches("NewPassword123",
	 * testUser.getPassword()); // Ensure new != old
	 * verify(passwordEncoder).encode("NewPassword123"); // Encode new password
	 * verify(userRepo).save(testUser); }
	 */


    @Test
    void changePassword_WithInvalidOldPassword_ShouldThrowException() {
        // Arrange
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername("testuser");
        request.setOldPassword("wrongPassword");
        request.setNewPassword("NewPassword123");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.changePassword("testuser", request)
        );

        assertTrue(exception.getMessage().contains("Old password is incorrect"));
        verify(userRepo).findByUsername("testuser");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void changePassword_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername("nonexistent");
        request.setOldPassword("oldPassword");
        request.setNewPassword("NewPassword123");

        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> userService.changePassword("nonexistent", request));

        verify(userRepo).findByUsername("nonexistent");
    }

    // ==================== updateProfile() Tests ====================

    @Test
    void updateProfile_WithValidData_ShouldUpdateProfile() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setPhone("9876543210");
        request.setDepartment("HR");
        request.setYearOfStudy("2025");
        request.setDob(LocalDate.of(1999, 5, 15));

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(profileRepo.save(any(UserProfile.class))).thenReturn(testProfile);

        // Act
        String result = userService.updateProfile("testuser", request);

        // Assert
        assertEquals("Profile updated successfully", result);
        assertEquals("Updated Name", testProfile.getFullName());
        assertEquals("9876543210", testProfile.getPhone());

        verify(userRepo).findByUsername("testuser");
        verify(profileRepo).findByUser(testUser);
        verify(profileRepo).save(testProfile);
    }

    @Test
    void updateProfile_WithNewProfile_ShouldCreateProfile() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New User");
        request.setPhone("1111111111");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.empty());
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = userService.updateProfile("testuser", request);

        // Assert
        assertEquals("Profile updated successfully", result);
        verify(userRepo).findByUsername("testuser");
        verify(profileRepo).findByUser(testUser);
        verify(profileRepo).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_WithProfilePicture_ShouldUploadImage() throws IOException {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
            "profilePic",
            "profile.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Test User");
        request.setProfilePic(mockFile);

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(profileRepo.save(any(UserProfile.class))).thenReturn(testProfile);

        // Act
        String result = userService.updateProfile("testuser", request);

        // Assert
        assertEquals("Profile updated successfully", result);
        assertNotNull(testProfile.getProfilePic());
        verify(profileRepo).save(testProfile);
    }

    @Test
    void updateProfile_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Test");

        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> userService.updateProfile("nonexistent", request));

        verify(userRepo).findByUsername("nonexistent");
        verify(profileRepo, never()).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_WithIOExceptionOnFileUpload_ShouldThrowException() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getBytes()).thenThrow(new IOException("File read error"));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Test");
        request.setProfilePic(mockFile);

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testProfile));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.updateProfile("testuser", request)
        );

        assertTrue(exception.getMessage().contains("Failed to read profile picture"));
    }

    // ==================== updateProfileFirstLogin() Tests ====================

    @Test
    void updateProfileFirstLogin_WithValidData_ShouldUpdateProfile() {
        // Arrange
        testUser.setFirstLogin(true);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("First Login User");
        request.setPhone("5555555555");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(profileRepo.save(any(UserProfile.class))).thenReturn(testProfile);

        // Act
        String result = userService.updateProfileFirstLogin("testuser", request);

        // Assert
        assertEquals("Profile updated successfully", result);
        assertEquals("First Login User", testProfile.getFullName());
        verify(userRepo).findByUsername("testuser");
        verify(profileRepo).save(testProfile);
    }

    @Test
    void updateProfileFirstLogin_WhenNotFirstLogin_ShouldThrowException() {
        // Arrange
        testUser.setFirstLogin(false);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Test");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.updateProfileFirstLogin("testuser", request)
        );

        assertTrue(exception.getMessage().contains("not allowed"));
        verify(userRepo).findByUsername("testuser");
        verify(profileRepo, never()).save(any(UserProfile.class));
    }

    // ==================== deactivateUser() Tests ====================

    @Test
    void deactivateUser_WithActiveUser_ShouldDeactivate() {
        // Arrange
        when(userRepo.findByUidAndEnabledTrue(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertFalse(testUser.isEnabled());
        assertNotNull(testUser.getDeactivatedAt());
        verify(userRepo).findByUidAndEnabledTrue(1L);
        verify(userRepo).save(testUser);
    }

    @Test
    void deactivateUser_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUidAndEnabledTrue(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.deactivateUser(999L)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepo).findByUidAndEnabledTrue(999L);
        verify(userRepo, never()).save(any(User.class));
    }

    // ==================== reactivateUser() Tests ====================

    @Test
    void reactivateUser_WithInactiveUser_ShouldReactivate() {
        // Arrange
        testUser.setEnabled(false);
        testUser.setDeactivatedAt(LocalDateTime.now());
        
        when(userRepo.findByUidAndEnabledFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.reactivateUser(1L);

        // Assert
        assertTrue(testUser.isEnabled());
        assertNull(testUser.getDeactivatedAt());
        verify(userRepo).findByUidAndEnabledFalse(1L);
        verify(userRepo).save(testUser);
    }

    @Test
    void reactivateUser_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUidAndEnabledFalse(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.reactivateUser(999L)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(userRepo).findByUidAndEnabledFalse(999L);
        verify(userRepo, never()).save(any(User.class));
    }

    // ==================== getProfile() Tests ====================

    @Test
    void getUserProfile_WithExistingProfile_ShouldReturnProfileData() {
        // Arrange
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserProfileResponse response = userService.getProfile("testuser");

        // Assert
        assertNotNull(response);
        assertEquals("Test User", response.getFullName());
        assertEquals("1234567890", response.getPhone());
        assertEquals("IT", response.getDepartment());

        verify(userRepo).findByUsername("testuser");
    }

    @Test
    void getUserProfile_WithNoProfile_ShouldCreateAndReturnBasicInfo() {
        // Arrange
        testUser.setProfile(null);
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(profileRepo.save(any(UserProfile.class))).thenAnswer(inv -> {
            UserProfile p = inv.getArgument(0);
            testUser.setProfile(p);
            return p;
        });

        // Act
        UserProfileResponse response = userService.getProfile("testuser");

        // Assert
        assertNotNull(response);
        verify(userRepo).findByUsername("testuser");
        verify(profileRepo).save(any(UserProfile.class));
    }

    @Test
    void getUserProfile_WithNonexistentUser_ShouldThrowException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> userService.getProfile("nonexistent"));

        verify(userRepo).findByUsername("nonexistent");
    }
}
