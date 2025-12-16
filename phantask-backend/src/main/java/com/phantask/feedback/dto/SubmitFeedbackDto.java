package com.phantask.feedback.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitFeedbackDto {

    @NotEmpty
    private Map<String, Integer> ratings;

	public Map<String, Integer> getRatings() {
		return ratings;
	}

	public void setRatings(Map<String, Integer> ratings) {
		this.ratings = ratings;
	}

    /* getters & setters */
    
    
}
