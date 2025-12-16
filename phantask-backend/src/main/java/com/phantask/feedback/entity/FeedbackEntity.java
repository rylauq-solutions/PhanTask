package com.phantask.feedback.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.phantask.task.entity.TaskStatus;

@Entity
@Getter
@Setter
@Table(name = "feedback")
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, updatable = false)
    private String feedbackCode;

    

	// Admin-created
    private String title;
    private String entityName;

    @Column(columnDefinition = "TEXT")
    private String assignedRolesJson;

    @Column(columnDefinition = "TEXT")
    private String questionsJson;

    private LocalDateTime createdAt;

    // Submission data
    private String submittedByUsername;
    private String submittedByRole;
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String ratingsJson;

	
    
    
}


