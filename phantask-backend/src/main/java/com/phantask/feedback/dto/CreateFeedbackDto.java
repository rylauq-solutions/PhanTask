package com.phantask.feedback.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.phantask.task.entity.TaskStatus;


@Data
public class CreateFeedbackDto {

    @NotEmpty
    private String title;

    @NotEmpty
    private String entityName;

    @NotEmpty
    private List<String> assignedRoles;

    @NotEmpty
    private List<String> questions;

	
    
    
}
