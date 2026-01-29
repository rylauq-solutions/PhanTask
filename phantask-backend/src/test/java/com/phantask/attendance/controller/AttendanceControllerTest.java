package com.phantask.attendance.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.phantask.attendance.dto.AttendancePercentageResponse;
import com.phantask.attendance.dto.AttendanceReportRequest;
import com.phantask.attendance.entity.Attendance;
import com.phantask.attendance.enums.AttendanceStatus;
import com.phantask.attendance.service.IAttendanceService;
import com.phantask.authentication.entity.User;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Integration tests for AttendanceController
 * 
 * Uses @SpringBootTest with @AutoConfigureMockMvc to properly test
 * controller behavior with security configuration
 */
@SpringBootTest
@AutoConfigureMockMvc
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAttendanceService attendanceService;

    private User testUser;
    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUid(1L);
        testUser.setUsername("testuser");

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setUser(testUser);
        testAttendance.setAttendanceDate(LocalDate.now());
        testAttendance.setStatus(AttendanceStatus.CHECKED_IN);
    }

    // ==================== POST /api/attendance/token/register Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void registerToken_WithValidToken_ShouldReturn200() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";
        doNothing().when(attendanceService).registerQrToken("valid-token");

        // Act & Assert
        mockMvc.perform(post("/api/attendance/token/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());

        verify(attendanceService).registerQrToken("valid-token");
    }

    @Test
    void registerToken_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";

        // Act & Assert
        mockMvc.perform(post("/api/attendance/token/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        		.andExpect(status().isForbidden());

        verify(attendanceService, never()).registerQrToken(anyString());
    }

    // ==================== POST /api/attendance/mark Tests ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithValidToken_ShouldReturn200() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";
        when(attendanceService.markAttendance("valid-token"))
                .thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attendance marked successfully"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.attendance").exists());

        verify(attendanceService).markAttendance("valid-token");
    }

    @Test
    @WithMockUser(authorities = "HR")
    void markAttendance_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";
        when(attendanceService.markAttendance("valid-token"))
                .thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MANAGER")
    void markAttendance_WithManagerRole_ShouldReturn200() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";
        when(attendanceService.markAttendance("valid-token"))
                .thenReturn(testAttendance);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void markAttendance_WithUserRole_ShouldReturn403() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isForbidden());

        verify(attendanceService, never()).markAttendance(anyString());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithEmptyToken_ShouldReturn400() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("QR token is required"));

        verify(attendanceService, never()).markAttendance(anyString());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithNullToken_ShouldReturn400() throws Exception {
        // Arrange
        String jsonRequest = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("QR token is required"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithExpiredToken_ShouldReturn403() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"expired-token\"}";
        when(attendanceService.markAttendance("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("QR code expired. User needs to refresh."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithInvalidToken_ShouldReturn400() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"invalid-token\"}";
        when(attendanceService.markAttendance("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void markAttendance_WithServiceException_ShouldReturn500() throws Exception {
        // Arrange
        String jsonRequest = "{\"token\":\"valid-token\"}";
        when(attendanceService.markAttendance("valid-token"))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/attendance/mark")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to mark attendance: Database error"));
    }

    // ==================== GET /api/attendance/my Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void getMyAttendance_WithAuthentication_ShouldReturn200() throws Exception {
        // Arrange
        List<Attendance> attendances = Arrays.asList(testAttendance);
        when(attendanceService.getMyAttendance()).thenReturn(attendances);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(attendanceService).getMyAttendance();
    }

    @Test
    void getMyAttendance_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/my"))
        		.andExpect(status().isForbidden());

        verify(attendanceService, never()).getMyAttendance();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getMyAttendance_WithNoRecords_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(attendanceService.getMyAttendance()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/attendance/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== GET /api/attendance/percentage/my Tests ====================

    @Test
    @WithMockUser(username = "testuser")
    void getMyAttendancePercentage_WithAuthentication_ShouldReturn200() throws Exception {
        // Arrange
        AttendancePercentageResponse response = new AttendancePercentageResponse(
            1L, "testuser", 10, 8, 2, 0, 80.0
        );
        when(attendanceService.getMyAttendancePercentage()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/attendance/percentage/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.totalDays").value(10))
                .andExpect(jsonPath("$.presentDays").value(8))
                .andExpect(jsonPath("$.attendancePercentage").value(80.0));

        verify(attendanceService).getMyAttendancePercentage();
    }

    @Test
    void getMyAttendancePercentage_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/attendance/percentage/my"))
        		.andExpect(status().isForbidden());
    }

    // ==================== POST /api/attendance/percentage/download Tests ====================

    @Test
    @WithMockUser(authorities = "ADMIN")
    void downloadAttendancePercentage_WithAdminRole_ShouldReturn200() throws Exception {
        // Arrange
        AttendanceReportRequest request = new AttendanceReportRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        
        List<AttendancePercentageResponse> data = Arrays.asList(
            new AttendancePercentageResponse(1L, "user1", 7, 6, 1, 0, 85.71)
        );
        
        when(attendanceService.getAttendancePercentage(any(), any(), any()))
                .thenReturn(data);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/percentage/download")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    "attachment; filename=attendance_percentage.csv"))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User ID")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Username")));

        verify(attendanceService).getAttendancePercentage(
            request.getStartDate(), 
            request.getEndDate(), 
            null
        );
    }

    @Test
    @WithMockUser(authorities = "HR")
    void downloadAttendancePercentage_WithHRRole_ShouldReturn200() throws Exception {
        // Arrange
        AttendanceReportRequest request = new AttendanceReportRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        request.setUserId(1L);
        
        when(attendanceService.getAttendancePercentage(any(), any(), eq(1L)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/api/attendance/percentage/download")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void downloadAttendancePercentage_WithUserRole_ShouldReturn403() throws Exception {
        // Arrange
        AttendanceReportRequest request = new AttendanceReportRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        // Act & Assert
        mockMvc.perform(post("/api/attendance/percentage/download")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(attendanceService, never()).getAttendancePercentage(any(), any(), any());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void downloadAttendancePercentage_ShouldIncludeAllCSVColumns() throws Exception {
        // Arrange
        AttendanceReportRequest request = new AttendanceReportRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());
        
        List<AttendancePercentageResponse> data = Arrays.asList(
            new AttendancePercentageResponse(1L, "user1", 7, 6, 1, 0, 85.71)
        );
        
        when(attendanceService.getAttendancePercentage(any(), any(), any()))
                .thenReturn(data);

        // Act & Assert
        mockMvc.perform(post("/api/attendance/percentage/download")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Total Days")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Present Days")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Absent Days")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Leave Days")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attendance Percentage")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1,user1,7,6,1,0,85.71")));
    }
}
