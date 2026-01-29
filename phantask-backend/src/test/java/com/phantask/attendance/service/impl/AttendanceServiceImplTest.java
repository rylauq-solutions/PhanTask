package com.phantask.attendance.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.phantask.attendance.dto.AttendancePercentageResponse;
import com.phantask.attendance.entity.Attendance;
import com.phantask.attendance.entity.AttendanceToken;
import com.phantask.attendance.enums.AttendanceStatus;
import com.phantask.attendance.repository.AttendanceRepository;
import com.phantask.attendance.repository.AttendanceTokenRepository;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.exception.AttendanceAlreadyMarkedException;

/**
 * Unit tests for AttendanceServiceImpl
 * 
 * Tests cover:
 * - QR token registration and validation
 * - Check-in/check-out logic
 * - Attendance percentage calculations
 * - Auto-absent marking
 * - Edge cases and error scenarios
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private AttendanceTokenRepository tokenRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private User testUser;
    private Attendance testAttendance;
    private AttendanceToken testToken;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUid(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(30));

        // Setup test attendance
        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setUser(testUser);
        testAttendance.setAttendanceDate(LocalDate.now());
        testAttendance.setStatus(AttendanceStatus.CHECKED_IN);

        // Setup test token
        testToken = new AttendanceToken();
        testToken.setId(1L);
        testToken.setToken("valid-jwt-token");
        testToken.setUser(testUser);
        testToken.setDate(LocalDate.now());
        testToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        testToken.setUsed(false);

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== registerQrToken() Tests ====================

    @Test
    void registerQrToken_WithValidUser_ShouldSaveToken() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act
        attendanceService.registerQrToken("new-token");

        // Assert
        verify(tokenRepo).invalidateActiveTokens(testUser, LocalDate.now());
        verify(tokenRepo).save(any(AttendanceToken.class));
    }

    @Test
    void registerQrToken_WhenAttendanceAlreadyCompleted_ShouldThrowException() {
        // Arrange
        testAttendance.setCheckOutTime(LocalDateTime.now());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        assertThrows(AttendanceAlreadyMarkedException.class, 
                () -> attendanceService.registerQrToken("new-token"));
        
        verify(tokenRepo, never()).save(any(AttendanceToken.class));
    }

    @Test
    void registerQrToken_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> attendanceService.registerQrToken("new-token"));
    }

    @Test
    void registerQrToken_WhenAlreadyCheckedIn_ShouldAllowTokenGeneration() {
        // Arrange
        testAttendance.setCheckInTime(LocalDateTime.now());
        testAttendance.setCheckOutTime(null); // Not checked out yet
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.of(testAttendance));

        // Act
        attendanceService.registerQrToken("new-token");

        // Assert - Should allow token generation for checkout
        verify(tokenRepo).save(any(AttendanceToken.class));
    }

    // ==================== markAttendance() Tests ====================

    @Test
    void markAttendance_WithValidToken_ShouldCheckInUser() {
        // Arrange
        when(tokenRepo.findByTokenAndUsedFalse("valid-token"))
                .thenReturn(Optional.of(testToken));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Attendance result = attendanceService.markAttendance("valid-token");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCheckInTime());
        assertNull(result.getCheckOutTime());
        assertEquals(AttendanceStatus.CHECKED_IN, result.getStatus());
        assertEquals(testUser, result.getUser());
        
        verify(tokenRepo).save(argThat(token -> token.isUsed()));
        verify(attendanceRepo).save(any(Attendance.class));
    }

    @Test
    void markAttendance_WithValidTokenSecondTime_ShouldCheckOutUser() {
        // Arrange
        testAttendance.setCheckInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setCheckOutTime(null);
        
        when(tokenRepo.findByTokenAndUsedFalse("valid-token"))
                .thenReturn(Optional.of(testToken));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.of(testAttendance));
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Attendance result = attendanceService.markAttendance("valid-token");

        // Assert
        assertNotNull(result.getCheckInTime());
        assertNotNull(result.getCheckOutTime());
        assertEquals(AttendanceStatus.CHECKED_OUT, result.getStatus());
        
        verify(tokenRepo).save(argThat(token -> token.isUsed()));
    }

    @Test
    void markAttendance_WithExpiredToken_ShouldThrowException() {
        // Arrange
        testToken.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired
        when(tokenRepo.findByTokenAndUsedFalse("expired-token"))
                .thenReturn(Optional.of(testToken));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> attendanceService.markAttendance("expired-token"));
        
        assertTrue(exception.getMessage().contains("expired"));
        verify(attendanceRepo, never()).save(any(Attendance.class));
    }

    @Test
    void markAttendance_WithInvalidToken_ShouldThrowException() {
        // Arrange
        when(tokenRepo.findByTokenAndUsedFalse("invalid-token"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> attendanceService.markAttendance("invalid-token"));
    }

    @Test
    void markAttendance_WhenAlreadyCompleted_ShouldThrowException() {
        // Arrange
        testAttendance.setCheckInTime(LocalDateTime.now().minusHours(8));
        testAttendance.setCheckOutTime(LocalDateTime.now());
        
        when(tokenRepo.findByTokenAndUsedFalse("valid-token"))
                .thenReturn(Optional.of(testToken));
        when(attendanceRepo.findByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(Optional.of(testAttendance));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> attendanceService.markAttendance("valid-token"));
        
        assertTrue(exception.getMessage().contains("already completed"));
    }

    @Test
    void markAttendance_ShouldMarkTokenAsUsed() {
        // Arrange
        when(tokenRepo.findByTokenAndUsedFalse("valid-token"))
                .thenReturn(Optional.of(testToken));
        when(attendanceRepo.findByUserAndAttendanceDate(any(), any()))
                .thenReturn(Optional.empty());
        when(attendanceRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        attendanceService.markAttendance("valid-token");

        // Assert
        verify(tokenRepo).save(argThat(token -> 
            token.isUsed() == true && token.getToken().equals("valid-jwt-token")
        ));
    }

    // ==================== getMyAttendance() Tests ====================

    @Test
    void getMyAttendance_WithValidUser_ShouldReturnAttendanceList() {
        // Arrange
        List<Attendance> expectedAttendances = Arrays.asList(testAttendance);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUser(testUser)).thenReturn(expectedAttendances);

        // Act
        List<Attendance> result = attendanceService.getMyAttendance();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAttendance, result.get(0));
        verify(attendanceRepo).findByUser(testUser);
    }

    @Test
    void getMyAttendance_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> attendanceService.getMyAttendance());
    }

    // ==================== getMyAttendancePercentage() Tests ====================

    @Test
    void getMyAttendancePercentage_WithNoRecords_ShouldReturnZeroPercentage() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserUidAndAttendanceDateBetween(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        AttendancePercentageResponse result = attendanceService.getMyAttendancePercentage();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalDays());
        assertEquals(0, result.getPresentDays());
        assertEquals(0.0, result.getAttendancePercentage());
    }

    @Test
    void getMyAttendancePercentage_WithMixedAttendance_ShouldCalculateCorrectly() {
        // Arrange
        List<Attendance> attendances = createMixedAttendanceList();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserUidAndAttendanceDateBetween(anyLong(), any(), any()))
                .thenReturn(attendances);

        // Act
        AttendancePercentageResponse result = attendanceService.getMyAttendancePercentage();

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalDays()); // Total records
        assertEquals(7, result.getPresentDays()); // CHECKED_OUT + WFH
        assertEquals(2, result.getAbsentDays());
        assertEquals(1, result.getLeaveDays());
        // Percentage = (7 / (10-1)) * 100 = 77.78%
        assertTrue(result.getAttendancePercentage() >= 77.0 && 
                   result.getAttendancePercentage() <= 78.0);
    }

    @Test
    void getMyAttendancePercentage_WithOnlyLeaveDays_ShouldReturnZeroPercentage() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(
            createAttendance(LocalDate.now().minusDays(1), AttendanceStatus.LEAVE),
            createAttendance(LocalDate.now().minusDays(2), AttendanceStatus.LEAVE)
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(attendanceRepo.findByUserUidAndAttendanceDateBetween(anyLong(), any(), any()))
                .thenReturn(attendances);

        // Act
        AttendancePercentageResponse result = attendanceService.getMyAttendancePercentage();

        // Assert
        assertEquals(2, result.getTotalDays());
        assertEquals(0, result.getPresentDays());
        assertEquals(2, result.getLeaveDays());
        assertEquals(0.0, result.getAttendancePercentage()); // No effective days
    }

    // ==================== getAttendancePercentage() Tests ====================

    @Test
    void getAttendancePercentage_ForAllUsers_ShouldReturnMultipleResponses() {
        // Arrange
        User user2 = new User();
        user2.setUid(2L);
        user2.setUsername("user2");
        
        List<Attendance> attendances = Arrays.asList(
            createAttendanceForUser(testUser, LocalDate.now(), AttendanceStatus.CHECKED_OUT),
            createAttendanceForUser(user2, LocalDate.now(), AttendanceStatus.CHECKED_OUT)
        );
        
        when(attendanceRepo.findByAttendanceDateBetween(any(), any()))
                .thenReturn(attendances);

        // Act
        List<AttendancePercentageResponse> result = 
            attendanceService.getAttendancePercentage(
                LocalDate.now().minusDays(7), 
                LocalDate.now(), 
                null
            );

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAttendancePercentage_ForSpecificUser_ShouldReturnSingleResponse() {
        // Arrange
        List<Attendance> attendances = Arrays.asList(
            createAttendance(LocalDate.now(), AttendanceStatus.CHECKED_OUT),
            createAttendance(LocalDate.now().minusDays(1), AttendanceStatus.CHECKED_OUT)
        );
        
        when(attendanceRepo.findByUserUidAndAttendanceDateBetween(eq(1L), any(), any()))
                .thenReturn(attendances);

        // Act
        List<AttendancePercentageResponse> result = 
            attendanceService.getAttendancePercentage(
                LocalDate.now().minusDays(7), 
                LocalDate.now(), 
                1L
            );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    // ==================== markAbsentUsers() Tests ====================

    @Test
    void markAbsentUsers_ShouldMarkMissingUsersAsAbsent() {
        // Arrange
        User user2 = new User();
        user2.setUid(2L);
        user2.setUsername("user2");
        user2.setEnabled(true);
        
        List<User> allUsers = Arrays.asList(testUser, user2);
        
        when(userRepo.findAllByEnabledTrue()).thenReturn(allUsers);
        when(attendanceRepo.existsByUserAndAttendanceDate(testUser, LocalDate.now()))
                .thenReturn(true); // testUser already marked
        when(attendanceRepo.existsByUserAndAttendanceDate(user2, LocalDate.now()))
                .thenReturn(false); // user2 not marked

        // Act
        attendanceService.markAbsentUsers();

        // Assert
        verify(attendanceRepo).save(argThat(attendance ->
            attendance.getUser().equals(user2) &&
            attendance.getStatus() == AttendanceStatus.ABSENT &&
            attendance.getAttendanceDate().equals(LocalDate.now())
        ));
        verify(attendanceRepo, times(1)).save(any(Attendance.class));
    }

    @Test
    void markAbsentUsers_WhenAllUsersMarked_ShouldNotSaveAny() {
        // Arrange
        List<User> allUsers = Arrays.asList(testUser);
        
        when(userRepo.findAllByEnabledTrue()).thenReturn(allUsers);
        when(attendanceRepo.existsByUserAndAttendanceDate(any(), any()))
                .thenReturn(true);

        // Act
        attendanceService.markAbsentUsers();

        // Assert
        verify(attendanceRepo, never()).save(any(Attendance.class));
    }

    @Test
    void markAbsentUsers_WithNoEnabledUsers_ShouldNotProcess() {
        // Arrange
        when(userRepo.findAllByEnabledTrue()).thenReturn(Collections.emptyList());

        // Act
        attendanceService.markAbsentUsers();

        // Assert
        verify(attendanceRepo, never()).existsByUserAndAttendanceDate(any(), any());
        verify(attendanceRepo, never()).save(any(Attendance.class));
    }

    // ==================== Helper Methods ====================

    private List<Attendance> createMixedAttendanceList() {
        return Arrays.asList(
            createAttendance(LocalDate.now(), AttendanceStatus.CHECKED_OUT),
            createAttendance(LocalDate.now().minusDays(1), AttendanceStatus.CHECKED_OUT),
            createAttendance(LocalDate.now().minusDays(2), AttendanceStatus.CHECKED_OUT),
            createAttendance(LocalDate.now().minusDays(3), AttendanceStatus.CHECKED_OUT),
            createAttendance(LocalDate.now().minusDays(4), AttendanceStatus.WFH),
            createAttendance(LocalDate.now().minusDays(5), AttendanceStatus.WFH),
            createAttendance(LocalDate.now().minusDays(6), AttendanceStatus.WFH),
            createAttendance(LocalDate.now().minusDays(7), AttendanceStatus.ABSENT),
            createAttendance(LocalDate.now().minusDays(8), AttendanceStatus.ABSENT),
            createAttendance(LocalDate.now().minusDays(9), AttendanceStatus.LEAVE)
        );
    }

    private Attendance createAttendance(LocalDate date, AttendanceStatus status) {
        return createAttendanceForUser(testUser, date, status);
    }

    private Attendance createAttendanceForUser(User user, LocalDate date, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        if (status == AttendanceStatus.CHECKED_OUT || status == AttendanceStatus.WFH) {
            attendance.setCheckInTime(date.atTime(9, 0));
            attendance.setCheckOutTime(date.atTime(18, 0));
        }
        return attendance;
    }
}
