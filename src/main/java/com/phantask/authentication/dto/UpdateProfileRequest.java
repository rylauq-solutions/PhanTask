package com.phantask.authentication.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {	
    private String fullName;
    private String department;
    private String phone;
    private String photoUrl;
    private String yearOfStudy;

}