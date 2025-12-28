package com.phantask.attendance.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.phantask.attendance.entity.AttendanceToken;
import com.phantask.authentication.entity.User;

public interface AttendanceTokenRepository extends JpaRepository<AttendanceToken, Long> {

    Optional<AttendanceToken> findByTokenAndUsedFalse(String token);
    boolean existsByUserUidAndDate(Long userId, LocalDate date);
    boolean existsByUserAndDate(User user, LocalDate date);
    void deleteByExpiresAtBefore(LocalDateTime now);
    
    @Modifying
    @Query("""
        UPDATE AttendanceToken t
        SET t.used = true
        WHERE t.user = :user
          AND t.date = :date
          AND t.used = false
    """)
    void invalidateActiveTokens(User user, LocalDate date);
}

