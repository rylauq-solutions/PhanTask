package com.phantask.attendance.enums;

public enum AttendanceStatus {
    CHECKED_IN,
    CHECKED_OUT,
    LEAVE,
    WFH,
    ABSENT;
    
    public boolean isPresent() {
        return this == CHECKED_IN || this == CHECKED_OUT || this == WFH;
    }

    public boolean isCompleted() {
        return this == CHECKED_OUT || this == ABSENT || this == LEAVE;
    }
}
