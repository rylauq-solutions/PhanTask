package com.phantask.feedback.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.phantask.task.entity.TaskStatus;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class FeedbackReportDto {

	private Map<String, Integer> averagePerQuestion; // rounded (1–5)
    private int overallAverage;                      // rounded (1–10)
    private int totalSubmissions;
    
    
	
    /* getters & setters */
    
}
