package com.phantask.attendance.service;

import java.time.LocalDate;
import java.util.List;

import com.phantask.attendance.dto.AttendancePercentageResponse;
import com.phantask.attendance.entity.Attendance;

public interface IAttendanceService {

	void registerQrToken(String token);
	Attendance markAttendance(String token);
	List<Attendance> getMyAttendance();
	AttendancePercentageResponse getMyAttendancePercentage();
	List<AttendancePercentageResponse> getAttendancePercentage(
	            LocalDate startDate,
	            LocalDate endDate,
	            Long userId);
}
