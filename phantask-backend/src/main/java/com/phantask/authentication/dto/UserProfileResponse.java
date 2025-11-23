package com.phantask.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;

    private String fullName;
    private String department;
    private String phone;
    private String photoUrl;
    private String yearOfStudy;
}

