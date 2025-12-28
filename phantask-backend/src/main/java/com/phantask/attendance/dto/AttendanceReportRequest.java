package com.phantask.attendance.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceReportRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId; // optional
}
