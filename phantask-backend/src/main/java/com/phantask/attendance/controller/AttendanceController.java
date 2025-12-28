package com.phantask.attendance.controller;

import java.util.List;

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
     * Admin scans user's QR token
     */
    @PostMapping("/mark")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @RequestBody MarkAttendanceRequest request) {

    	Attendance attendance = attendanceService.markAttendance(request.getToken());

        return ResponseEntity.ok(new AttendanceResponse(attendance));
    }

    /**
     * User views own attendance
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Attendance>> myAttendance() {
        return ResponseEntity.ok(attendanceService.getMyAttendance());
    }
    
    @PostMapping("/percentage/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
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