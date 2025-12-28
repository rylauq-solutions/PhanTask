package com.phantask.attendance.dto;

import java.time.LocalDate;

import com.phantask.attendance.entity.Attendance;
import com.phantask.attendance.enums.AttendanceUiState;

import lombok.Getter;

@Getter
public class AttendanceResponse {

	private LocalDate date;
	private AttendanceUiState state;

    public AttendanceResponse(Attendance attendance) {
        this.date = attendance.getAttendanceDate();
        this.state = deriveState(attendance);
    }
    
    /**
     * Derives UI-friendly attendance state from attendance timestamps.
     */
    private AttendanceUiState deriveState(Attendance attendance) {
    	
    	// User has not checked in yet
        if (attendance.getCheckInTime() == null) {
            return AttendanceUiState.NOT_CHECKED_IN;
        }
        // User checked in but not checked out
        if (attendance.getCheckOutTime() == null) {
            return AttendanceUiState.CHECKED_IN;
        }
        // User checked out → attendance finished
        return AttendanceUiState.COMPLETED;
    }
}
