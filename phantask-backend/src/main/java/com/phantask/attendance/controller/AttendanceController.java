package com.phantask.attendance.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.phantask.attendance.dto.AttendancePercentageResponse;
import com.phantask.attendance.dto.AttendanceReportRequest;
import com.phantask.attendance.dto.AttendanceResponse;
import com.phantask.attendance.dto.MarkAttendanceRequest;
import com.phantask.attendance.entity.Attendance;
import com.phantask.attendance.service.IAttendanceService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;

    /**
     * User navigates to Attendance for generating a QR-code
     */
    @PostMapping("/token/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerToken(
            @RequestBody MarkAttendanceRequest request) {

        attendanceService.registerQrToken(request.getToken());
        return ResponseEntity.ok().build();
    }
    
    /**
     * Admin/HR/Manager scans user's QR token to mark attendance
     * 
     * CHANGED: hasRole() → hasAuthority()
     * Reason: JWT tokens use "ADMIN" not "ROLE_ADMIN"
     */
    @PostMapping("/mark")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR') or hasAuthority('MANAGER')")
    public ResponseEntity<?> markAttendance(
            @RequestBody MarkAttendanceRequest request) {

        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "QR token is required"));
            }

            Attendance attendance = attendanceService.markAttendance(request.getToken());

            return ResponseEntity.ok(Map.of(
                "message", "Attendance marked successfully",
                "username", attendance.getUser().getUsername(),
                "timestamp", LocalDateTime.now(),
                "attendance", new AttendanceResponse(attendance)
            ));
            
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "QR code expired. User needs to refresh."));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid QR token: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark attendance: " + e.getMessage()));
        }
    }

    /**
     * User views own attendance
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Attendance>> myAttendance() {
        return ResponseEntity.ok(attendanceService.getMyAttendance());
    }
    
    @GetMapping("/percentage/my")
    @PreAuthorize("isAuthenticated()")
    public AttendancePercentageResponse getMyAttendancePercentage() {
        return attendanceService.getMyAttendancePercentage();
    }
    
    /**
     * Admin/HR downloads attendance report
     * 
     * CHANGED: hasRole() → hasAuthority()
     */
    @PostMapping("/percentage/download")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<byte[]> downloadAttendancePercentage(
            @RequestBody AttendanceReportRequest request
    ) {

        List<AttendancePercentageResponse> data =
                attendanceService.getAttendancePercentage(
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getUserId()
                );

        String csv = buildCsv(data);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=attendance_percentage.csv")
                .header("Content-Type", "text/csv")
                .body(csv.getBytes());
    }
    
    private String buildCsv(List<AttendancePercentageResponse> data) {

        StringBuilder sb = new StringBuilder();
        sb.append("User ID,Username,Total Days,Present Days,Absent Days,Leave Days,Attendance Percentage\n");

        for (AttendancePercentageResponse r : data) {
            sb.append(r.getUserId()).append(",")
              .append(r.getUsername()).append(",")
              .append(r.getTotalDays()).append(",")
              .append(r.getPresentDays()).append(",")
              .append(r.getAbsentDays()).append(",")
              .append(r.getLeaveDays()).append(",")
              .append(r.getAttendancePercentage())
              .append("\n");
        }

        return sb.toString();
    }
}
