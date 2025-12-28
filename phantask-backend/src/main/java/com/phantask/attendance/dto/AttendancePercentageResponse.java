package com.phantask.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendancePercentageResponse {

    private Long userId;
    private String username;

    private long totalDays;
    private long presentDays;
    private long absentDays;
    private long leaveDays;

    private double attendancePercentage;
}
