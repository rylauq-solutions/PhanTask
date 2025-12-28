package com.phantask.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.phantask.attendance.dto.AttendancePercentageResponse;
import com.phantask.attendance.entity.Attendance;
import com.phantask.attendance.entity.AttendanceToken;
import com.phantask.attendance.enums.AttendanceStatus;
import com.phantask.attendance.repository.AttendanceRepository;
import com.phantask.attendance.repository.AttendanceTokenRepository;
import com.phantask.attendance.service.IAttendanceService;
import com.phantask.authentication.entity.User;
import com.phantask.authentication.repository.UserRepository;
import com.phantask.exception.AttendanceAlreadyMarkedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final AttendanceTokenRepository tokenRepo;
    private final UserRepository userRepo;

    /**
     * Generates a new QR token for the logged-in user to mark attendance.
     * Blocks token generation if attendance is already completed for the day.
     * Invalidates any previously active tokens for today.
     */
    @Override
    @Transactional
    public void registerQrToken(String token) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepo
                .findByUserAndAttendanceDate(user, today)
                .orElse(null);

        if (attendance != null && attendance.getCheckOutTime() != null) {
        	throw new AttendanceAlreadyMarkedException("Attendance already marked for today");
        }
        
        tokenRepo.invalidateActiveTokens(user, today);

        AttendanceToken attendanceToken = new AttendanceToken();
        attendanceToken.setToken(token);
        attendanceToken.setUser(user);
        attendanceToken.setDate(today);
        attendanceToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        attendanceToken.setUsed(false);

        tokenRepo.save(attendanceToken);
    }

    /**
     * Marks attendance using a scanned QR token.
     * First scan checks the user in, second scan checks the user out.
     * Further scans are rejected and the token is invalidated after use.
     */
    @Override
    @Transactional
    public Attendance markAttendance(String token) {

        AttendanceToken attendanceToken = tokenRepo
        		.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid or used QR token"));

        if (attendanceToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("QR token expired");
        }

        User user = attendanceToken.getUser();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepo
                .findByUserAndAttendanceDate(user, today)
                .orElse(null);

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setAttendanceDate(today);
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setStatus(AttendanceStatus.CHECKED_IN);
        } else if (attendance.getCheckOutTime() == null) {
            attendance.setCheckOutTime(LocalDateTime.now());
            attendance.setStatus(AttendanceStatus.CHECKED_OUT);
        } else {
            throw new RuntimeException("Attendance already completed");
        }

        attendanceToken.setUsed(true);
        tokenRepo.save(attendanceToken);
        
        log.info("Attendance marked for {}", user.getUsername());   
        return attendanceRepo.save(attendance); //create or update the attendance record
    }

    /**
     * Returns all attendance records of the currently logged-in user.
     */
    @Override
    public List<Attendance> getMyAttendance() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return attendanceRepo.findByUser(user);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public AttendancePercentageResponse getMyAttendancePercentage() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startDate = user.getCreatedAt().toLocalDate(); // or joining date
        LocalDate endDate = LocalDate.now();

        List<Attendance> records = attendanceRepo
                .findByUserUidAndAttendanceDateBetween(user.getUid(), startDate, endDate);

        if (records.isEmpty()) {
            return new AttendancePercentageResponse(
                    user.getUid(),
                    user.getUsername(),
                    0, 0, 0, 0, 0
            );
        }

        return calculatePercentage(records);
    }

    
    /**
     * System marked for No-Shows
     */
    @Scheduled(cron = "0 5 23 * * ?") // 11:05 PM daily
    @Transactional
    public void markAbsentUsers() {

        LocalDate today = LocalDate.now();

        List<User> users = userRepo.findAllByEnabledTrue();

        for (User user : users) {
            boolean exists = attendanceRepo
                .existsByUserAndAttendanceDate(user, today);

            if (!exists) {
                Attendance attendance = new Attendance();
                attendance.setUser(user);
                attendance.setAttendanceDate(today);
                attendance.setStatus(AttendanceStatus.ABSENT);
                attendanceRepo.save(attendance);
            }
        }
    }

    /**
     * Calculates attendance percentage for users within a given date range.
     * Can be filtered by userId or computed for all users (HR/Admin use).
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttendancePercentageResponse> getAttendancePercentage(
            LocalDate startDate, LocalDate endDate, Long userId) {

        List<Attendance> attendances;

        if (userId != null) {
            attendances = attendanceRepo
                    .findByUserUidAndAttendanceDateBetween(userId, startDate, endDate);
        } else {
            attendances = attendanceRepo
                    .findByAttendanceDateBetween(startDate, endDate);
        }

        // Group by user
        return attendances.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getUid()))
                .values()
                .stream()
                .map(this::calculatePercentage)
                .toList();
    }
    
    /**
     * Computes attendance percentage from attendance records of a single user.
     * Excludes leave days from the percentage calculation.
     */
    private AttendancePercentageResponse calculatePercentage(
            List<Attendance> records) {

        long totalDays = records.size();

        long presentDays = records.stream()
                .filter(a -> a.getStatus().isPresent())
                .count();

        long absentDays = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        long leaveDays = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LEAVE)
                .count();

        // Usually LEAVE is excluded from denominator
        long effectiveDays = totalDays - leaveDays;

        double percentage = effectiveDays == 0
                ? 0
                : (presentDays * 100.0) / effectiveDays;

        Attendance any = records.get(0);

        return new AttendancePercentageResponse(
                any.getUser().getUid(),
                any.getUser().getUsername(),
                totalDays,
                presentDays,
                absentDays,
                leaveDays,
                Math.round(percentage * 100.0) / 100.0
        );
    }

}
