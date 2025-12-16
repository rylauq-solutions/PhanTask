package com.phantask.feedback.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.phantask.feedback.dto.CreateFeedbackDto;
import com.phantask.feedback.dto.FeedbackReportDto;
import com.phantask.feedback.dto.SubmitFeedbackDto;
import com.phantask.feedback.entity.FeedbackEntity;
import com.phantask.feedback.repository.FeedbackRepository;
import com.phantask.feedback.util.JsonUtil;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

   

    /* ================= USER ================= */

    public List<FeedbackEntity> getAvailableFeedbackForUser(
            String username,
            List<String> userRoles
    ) {

        List<FeedbackEntity> baseFeedbacks =
                repository.findBySubmittedByUsernameIsNull();

        List<FeedbackEntity> result = new ArrayList<>();

        for (FeedbackEntity feedback : baseFeedbacks) {

            // 1️⃣ Role check
            List<String> assignedRoles = JsonUtil.fromJson(
                    feedback.getAssignedRolesJson(),
                    new TypeReference<List<String>>() {}
            );

            boolean roleAllowed =
                    assignedRoles.contains("All Employees")
                    || userRoles.stream().anyMatch(assignedRoles::contains);

            if (!roleAllowed) continue;

            // 2️⃣ Already submitted?
            boolean alreadySubmitted =
            	    repository.existsByFeedbackCodeAndSubmittedByUsername(
            	        feedback.getFeedbackCode(),
            	        username
            	    );

            if (!alreadySubmitted) {
                result.add(feedback);
            }
        }

        return result;
    }

    public void submitFeedback(
            Long feedbackId,
            SubmitFeedbackDto dto,
            String username,
            List<String> userRoles
    ) {

    	 // ✅ ENSURE ID BELONGS TO TEMPLATE
    	FeedbackEntity base = repository
    	        .findByIdAndSubmittedByUsernameIsNull(feedbackId)
    	        .orElseThrow(() ->
    	            new RuntimeException("Invalid feedback template ID")
    	        );

    	  // ✅ PREVENT DUPLICATE SUBMISSION (USING feedbackCode)
        if (repository.existsByFeedbackCodeAndSubmittedByUsername(
                base.getFeedbackCode(),
                username
        )) {
            throw new RuntimeException("Feedback already submitted by user");
        }

        // Role validation
        List<String> assignedRoles = JsonUtil.fromJson(
                base.getAssignedRolesJson(),
                new TypeReference<List<String>>() {}
        );

        boolean roleAllowed =
                assignedRoles.contains("All Employees")
                || userRoles.stream().anyMatch(assignedRoles::contains);

        if (!roleAllowed) {
            throw new RuntimeException("User role not allowed to submit this feedback");
        }

        FeedbackEntity submission = new FeedbackEntity();
        submission.setFeedbackCode(base.getFeedbackCode());
        submission.setTitle(base.getTitle());
        submission.setEntityName(base.getEntityName());
        submission.setQuestionsJson(base.getQuestionsJson());
        submission.setAssignedRolesJson(base.getAssignedRolesJson());
        submission.setCreatedAt(base.getCreatedAt());

        submission.setSubmittedByUsername(username);
        submission.setSubmittedByRole(userRoles.get(0));
        submission.setSubmittedAt(LocalDateTime.now());

        // Time validation
        if (submission.getSubmittedAt().isBefore(base.getCreatedAt())) {
            throw new RuntimeException("Submission time cannot be before creation time");
        }

        submission.setRatingsJson(JsonUtil.toJson(dto.getRatings()));

        repository.save(submission);
    }
    
    
    
    
    /* ================= ADMIN ================= */

    public void createFeedback(CreateFeedbackDto dto) {

        FeedbackEntity entity = new FeedbackEntity();
        entity.setFeedbackCode(UUID.randomUUID().toString());
        entity.setTitle(dto.getTitle());
        entity.setEntityName(dto.getEntityName());
        entity.setAssignedRolesJson(JsonUtil.toJson(dto.getAssignedRoles()));
        entity.setQuestionsJson(JsonUtil.toJson(dto.getQuestions()));
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);
    }
    
    
    
    /*===========Count of feedback submitted by each user======*/
    public long getSubmittedFeedbackCount(String username) {
        return repository.countBySubmittedByUsername(username);
    }

    
    
    /*========Admin can get all feedback template created by him=======*/
   
    public List<FeedbackEntity> getAllFeedbackTemplates() {
        return repository.findBySubmittedByUsernameIsNull();
    }
    

    /* ================= ADMIN REPORT ================= */

    public FeedbackReportDto getReport(Long feedbackId) {

        FeedbackEntity base = repository
                .findByIdAndSubmittedByUsernameIsNull(feedbackId)
                .orElseThrow(() ->
                        new RuntimeException("Invalid feedback template ID")
                );

        List<FeedbackEntity> submissions =
                repository.findByFeedbackCodeAndSubmittedByUsernameIsNotNull(
                        base.getFeedbackCode()
                );

        Map<String, Double> sumMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        for (FeedbackEntity submission : submissions) {

            Map<String, Integer> ratings = JsonUtil.fromJson(
                    submission.getRatingsJson(),
                    new TypeReference<Map<String, Integer>>() {}
            );

            for (Map.Entry<String, Integer> entry : ratings.entrySet()) {
                sumMap.merge(entry.getKey(), entry.getValue().doubleValue(), Double::sum);
                countMap.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        // Average per question (double)
        Map<String, Double> avgMap = new HashMap<>();
        for (String question : sumMap.keySet()) {
            avgMap.put(
                    question,
                    sumMap.get(question) / countMap.get(question)
            );
        }

        // Overall average (5-scale)
        double overallAvg5 = avgMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Scale to 10
        double overallAvg10 = overallAvg5 * 2;

        // Round AFTER calculation
        Map<String, Integer> roundedAvgPerQuestion = new HashMap<>();
        for (Map.Entry<String, Double> entry : avgMap.entrySet()) {
            roundedAvgPerQuestion.put(
                    entry.getKey(),
                    (int) Math.round(entry.getValue())
            );
        }

        int roundedOverallAvg = (int) Math.round(overallAvg10);

        FeedbackReportDto dto = new FeedbackReportDto();
        dto.setAveragePerQuestion(roundedAvgPerQuestion);
        dto.setOverallAverage(roundedOverallAvg);
        dto.setTotalSubmissions(submissions.size());

        return dto;
    }
    
    
    
    
    
    /* ================= UPDATION OF FEEDBACK TEMPLATE BY ADMIN  ================= */
    public void updateFeedback(Long templateId, CreateFeedbackDto dto) {

        FeedbackEntity template = repository
                .findByIdAndSubmittedByUsernameIsNull(templateId)
                .orElseThrow(() -> new RuntimeException("Invalid feedback template"));

        boolean hasSubmissions =
                repository.existsByFeedbackCodeAndSubmittedByUsernameIsNotNull(
                        template.getFeedbackCode()
                );

        if (hasSubmissions) {
            throw new RuntimeException("Cannot update feedback after submissions");
        }

        template.setTitle(dto.getTitle());
        template.setEntityName(dto.getEntityName());
        template.setAssignedRolesJson(JsonUtil.toJson(dto.getAssignedRoles()));
        template.setQuestionsJson(JsonUtil.toJson(dto.getQuestions()));

        repository.save(template);
    }

    
    /*========Deleting feedback deletes template + all submissions=======*/
    @Transactional
    public void deleteFeedback(Long templateId) {

        FeedbackEntity template = repository
                .findByIdAndSubmittedByUsernameIsNull(templateId)
                .orElseThrow(() -> new RuntimeException("Invalid feedback template"));

        repository.deleteByFeedbackCode(template.getFeedbackCode());
    }
    
    
    
    
}
