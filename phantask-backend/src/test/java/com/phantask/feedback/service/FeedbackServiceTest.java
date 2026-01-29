package com.phantask.feedback.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.phantask.feedback.dto.CreateFeedbackDto;
import com.phantask.feedback.dto.FeedbackReportDto;
import com.phantask.feedback.dto.FeedbackSummaryDto;
import com.phantask.feedback.dto.SubmitFeedbackDto;
import com.phantask.feedback.entity.Feedback;
import com.phantask.feedback.entity.Rating;
import com.phantask.feedback.entity.Submission;
import com.phantask.feedback.repository.FeedbackRepository;
import com.phantask.feedback.repository.RatingRepository;
import com.phantask.feedback.repository.SubmissionRepository;

/**
 * Comprehensive tests for FeedbackService
 * Tests admin operations, user submissions, and reporting
 */
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepo;

    @Mock
    private SubmissionRepository submissionRepo;

    @Mock
    private RatingRepository ratingRepo;

    @InjectMocks
    private FeedbackService feedbackService;

    private Feedback feedback;
    private CreateFeedbackDto createDto;
    private SubmitFeedbackDto submitDto;

    @BeforeEach
    void setUp() {
        // Setup feedback entity
        feedback = new Feedback();
        feedback.setFeedbackId(1L);
        feedback.setTitle("Q4 Performance Feedback");
        feedback.setAssignedRoles("EMPLOYEE,MANAGER");
        feedback.setQuestions("How satisfied are you?,Rate communication");
        feedback.setCreatedAt(LocalDateTime.now());

        // Setup create DTO
        createDto = new CreateFeedbackDto();
        createDto.setTitle("Q4 Performance Feedback");
        createDto.setAssignedRoles(Arrays.asList("EMPLOYEE", "MANAGER"));
        createDto.setQuestions(Arrays.asList("How satisfied are you?", "Rate communication"));

        // Setup submit DTO
        submitDto = new SubmitFeedbackDto();
        Map<String, Integer> ratings = new HashMap<>();
        ratings.put("How satisfied are you?", 5);
        ratings.put("Rate communication", 4);
        submitDto.setRatings(ratings);
    }

    // ==================== CREATE FEEDBACK Tests ====================

    @Test
    void createFeedback_WithValidData_ShouldSaveFeedback() {
        // Arrange
        when(feedbackRepo.save(any(Feedback.class))).thenReturn(feedback);

        // Act
        feedbackService.createFeedback(createDto);

        // Assert
        verify(feedbackRepo).save(argThat(f ->
            f.getTitle().equals("Q4 Performance Feedback") &&
            f.getAssignedRoles().equals("EMPLOYEE,MANAGER") &&
            f.getQuestions().equals("How satisfied are you?,Rate communication")
        ));
    }

    @Test
    void createFeedback_ShouldStoreRolesAsCSV() {
        // Arrange
        when(feedbackRepo.save(any(Feedback.class))).thenReturn(feedback);

        // Act
        feedbackService.createFeedback(createDto);

        // Assert
        verify(feedbackRepo).save(argThat(f ->
            f.getAssignedRoles().contains("EMPLOYEE") &&
            f.getAssignedRoles().contains("MANAGER")
        ));
    }

    @Test
    void createFeedback_ShouldStoreQuestionsAsCSV() {
        // Arrange
        when(feedbackRepo.save(any(Feedback.class))).thenReturn(feedback);

        // Act
        feedbackService.createFeedback(createDto);

        // Assert
        verify(feedbackRepo).save(argThat(f ->
            f.getQuestions().contains("How satisfied are you?") &&
            f.getQuestions().contains("Rate communication")
        ));
    }

    @Test
    void createFeedback_ShouldSetCreatedAtTimestamp() {
        // Arrange
        when(feedbackRepo.save(any(Feedback.class))).thenReturn(feedback);

        // Act
        feedbackService.createFeedback(createDto);

        // Assert
        verify(feedbackRepo).save(argThat(f -> f.getCreatedAt() != null));
    }

    // ==================== GET ALL FEEDBACK SUMMARIES Tests ====================

    @Test
    void getAllFeedbackSummaries_WithExistingFeedback_ShouldReturnSummaries() {
        // Arrange
        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(5L);

        // Act
        List<FeedbackSummaryDto> results = feedbackService.getAllFeedbackSummaries();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Q4 Performance Feedback", results.get(0).getTitle());
        assertEquals(5L, results.get(0).getSubmissionCount());
    }

    @Test
    void getAllFeedbackSummaries_ShouldConvertCSVToList() {
        // Arrange
        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(0L);

        // Act
        List<FeedbackSummaryDto> results = feedbackService.getAllFeedbackSummaries();

        // Assert
        assertEquals(2, results.get(0).getAssignedRoles().size());
        assertTrue(results.get(0).getAssignedRoles().contains("EMPLOYEE"));
        assertTrue(results.get(0).getAssignedRoles().contains("MANAGER"));
    }

    @Test
    void getAllFeedbackSummaries_WithNoFeedback_ShouldReturnEmptyList() {
        // Arrange
        when(feedbackRepo.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<FeedbackSummaryDto> results = feedbackService.getAllFeedbackSummaries();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getAllFeedbackSummaries_ShouldIncludeAllFields() {
        // Arrange
        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(3L);

        // Act
        List<FeedbackSummaryDto> results = feedbackService.getAllFeedbackSummaries();

        // Assert
        FeedbackSummaryDto summary = results.get(0);
        assertNotNull(summary.getFeedbackId());
        assertNotNull(summary.getTitle());
        assertNotNull(summary.getAssignedRoles());
        assertNotNull(summary.getQuestions());
        assertNotNull(summary.getCreatedAt());
        assertEquals(3L, summary.getSubmissionCount());
    }

    // ==================== UPDATE FEEDBACK Tests ====================

    @Test
    void updateFeedback_WithNoSubmissions_ShouldUpdateSuccessfully() {
        // Arrange
        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(0L);
        when(feedbackRepo.save(any(Feedback.class))).thenReturn(feedback);

        CreateFeedbackDto updateDto = new CreateFeedbackDto();
        updateDto.setTitle("Updated Title");
        updateDto.setAssignedRoles(Arrays.asList("ADMIN"));
        updateDto.setQuestions(Arrays.asList("New Question"));

        // Act
        feedbackService.updateFeedback(1L, updateDto);

        // Assert
        verify(feedbackRepo).save(argThat(f ->
            f.getTitle().equals("Updated Title") &&
            f.getAssignedRoles().equals("ADMIN") &&
            f.getQuestions().equals("New Question")
        ));
    }

    @Test
    void updateFeedback_WithExistingSubmissions_ShouldThrowException() {
        // Arrange
        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(5L);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.updateFeedback(1L, createDto)
        );
        verify(feedbackRepo, never()).save(any(Feedback.class));
    }

    @Test
    void updateFeedback_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(feedbackRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.updateFeedback(999L, createDto)
        );
    }

    // ==================== DELETE FEEDBACK Tests ====================

    @Test
    void deleteFeedback_ShouldDeleteAllRelatedData() {
        // Arrange
        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));

        // Act
        feedbackService.deleteFeedback(1L);

        // Assert
        verify(submissionRepo).deleteByFeedback(feedback);
        verify(ratingRepo).deleteByFeedback(feedback);
        verify(feedbackRepo).delete(feedback);
    }

    @Test
    void deleteFeedback_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(feedbackRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.deleteFeedback(999L)
        );
        verify(submissionRepo, never()).deleteByFeedback(any());
        verify(ratingRepo, never()).deleteByFeedback(any());
        verify(feedbackRepo, never()).delete(any());
    }

    @Test
    void deleteFeedback_ShouldDeleteInCorrectOrder() {
        // Arrange
        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));

        // Act
        feedbackService.deleteFeedback(1L);

        // Assert - verify order: submissions → ratings → feedback
        var inOrder = inOrder(submissionRepo, ratingRepo, feedbackRepo);
        inOrder.verify(submissionRepo).deleteByFeedback(feedback);
        inOrder.verify(ratingRepo).deleteByFeedback(feedback);
        inOrder.verify(feedbackRepo).delete(feedback);
    }

    // ==================== GET AVAILABLE FEEDBACK Tests ====================

    @Test
    void getAvailableFeedbackForUser_WithMatchingRole_ShouldReturnFeedback() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(false);

        // Act
        List<Feedback> results = feedbackService.getAvailableFeedbackForUser(username, userRoles);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getAvailableFeedbackForUser_WithNoMatchingRole_ShouldReturnEmpty() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));

        // Act
        List<Feedback> results = feedbackService.getAvailableFeedbackForUser(username, userRoles);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getAvailableFeedbackForUser_AlreadySubmitted_ShouldExclude() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(true);

        // Act
        List<Feedback> results = feedbackService.getAvailableFeedbackForUser(username, userRoles);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getAvailableFeedbackForUser_WithMultipleRoles_ShouldMatch() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("HR", "MANAGER");

        when(feedbackRepo.findAll()).thenReturn(Arrays.asList(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(false);

        // Act
        List<Feedback> results = feedbackService.getAvailableFeedbackForUser(username, userRoles);

        // Assert
        assertEquals(1, results.size());
    }

    // ==================== SUBMIT FEEDBACK Tests ====================

    @Test
    void submitFeedback_WithValidData_ShouldSaveSubmissionAndRatings() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(false);
        when(submissionRepo.save(any(Submission.class))).thenReturn(new Submission());
        when(ratingRepo.save(any(Rating.class))).thenReturn(new Rating());

        // Act
        feedbackService.submitFeedback(1L, submitDto, username, userRoles);

        // Assert
        verify(submissionRepo).save(any(Submission.class));
        verify(ratingRepo, times(2)).save(any(Rating.class));
    }

    @Test
    void submitFeedback_WithoutMatchingRole_ShouldThrowException() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.submitFeedback(1L, submitDto, username, userRoles)
        );
        verify(submissionRepo, never()).save(any());
        verify(ratingRepo, never()).save(any());
    }

    @Test
    void submitFeedback_AlreadySubmitted_ShouldThrowException() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.submitFeedback(1L, submitDto, username, userRoles)
        );
        verify(submissionRepo, never()).save(any());
    }

    @Test
    void submitFeedback_WithInvalidFeedbackId_ShouldThrowException() {
        // Arrange
        when(feedbackRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.submitFeedback(999L, submitDto, "john", Arrays.asList("EMPLOYEE"))
        );
    }

    @Test
    void submitFeedback_ShouldSaveSubmissionWithCorrectFields() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(false);
        when(submissionRepo.save(any(Submission.class))).thenReturn(new Submission());
        when(ratingRepo.save(any(Rating.class))).thenReturn(new Rating());

        // Act
        feedbackService.submitFeedback(1L, submitDto, username, userRoles);

        // Assert
        verify(submissionRepo).save(argThat(s ->
            s.getFeedback().equals(feedback) &&
            s.getUsername().equals(username) &&
            s.getUserRole().equals("EMPLOYEE") &&
            s.getSubmittedAt() != null
        ));
    }

    @Test
    void submitFeedback_ShouldSaveAllRatings() {
        // Arrange
        String username = "john";
        List<String> userRoles = Arrays.asList("EMPLOYEE");

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(submissionRepo.existsByFeedbackAndUsername(feedback, username)).thenReturn(false);
        when(submissionRepo.save(any(Submission.class))).thenReturn(new Submission());
        when(ratingRepo.save(any(Rating.class))).thenReturn(new Rating());

        // Act
        feedbackService.submitFeedback(1L, submitDto, username, userRoles);

        // Assert
        verify(ratingRepo, times(submitDto.getRatings().size())).save(argThat(r ->
            r.getFeedback() != null &&
            r.getFeedback().equals(feedback) &&
            r.getQuestion() != null &&
            r.getRating() > 0
        ));
    }

    // ==================== GET REPORT Tests ====================

    @Test
    void getReport_WithRatings_ShouldCalculateAverages() {
        // Arrange
        Rating rating1 = new Rating();
        rating1.setQuestion("How satisfied are you?");
        rating1.setRating(5);

        Rating rating2 = new Rating();
        rating2.setQuestion("How satisfied are you?");
        rating2.setRating(3);

        Rating rating3 = new Rating();
        rating3.setQuestion("Rate communication");
        rating3.setRating(4);

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(ratingRepo.findByFeedback(feedback)).thenReturn(Arrays.asList(rating1, rating2, rating3));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(2L);

        // Act
        FeedbackReportDto report = feedbackService.getReport(1L);

        // Assert
        assertNotNull(report);
        assertEquals(4, report.getAveragePerQuestion().get("How satisfied are you?")); // (5+3)/2 = 4
        assertEquals(4, report.getAveragePerQuestion().get("Rate communication")); // 4/1 = 4
        assertEquals(2L, report.getTotalSubmissions());
    }

    @Test
    void getReport_ShouldScaleOverallAverageTo10() {
        // Arrange
        Rating rating1 = new Rating();
        rating1.setQuestion("Q1");
        rating1.setRating(5);

        Rating rating2 = new Rating();
        rating2.setQuestion("Q2");
        rating2.setRating(3);

        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(ratingRepo.findByFeedback(feedback)).thenReturn(Arrays.asList(rating1, rating2));
        when(submissionRepo.countByFeedback(feedback)).thenReturn(1L);

        // Act
        FeedbackReportDto report = feedbackService.getReport(1L);

        // Assert
        // Average of 5 and 3 = 4, scaled to 10 = 8
        assertEquals(8, report.getOverallAverage());
    }

    @Test
    void getReport_WithNoRatings_ShouldReturnZeroAverage() {
        // Arrange
        when(feedbackRepo.findById(1L)).thenReturn(Optional.of(feedback));
        when(ratingRepo.findByFeedback(feedback)).thenReturn(Collections.emptyList());
        when(submissionRepo.countByFeedback(feedback)).thenReturn(0L);

        // Act
        FeedbackReportDto report = feedbackService.getReport(1L);

        // Assert
        assertEquals(0, report.getOverallAverage());
        assertTrue(report.getAveragePerQuestion().isEmpty());
    }

    @Test
    void getReport_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(feedbackRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            feedbackService.getReport(999L)
        );
    }

    // ==================== GET SUBMITTED FEEDBACK COUNT Tests ====================

    @Test
    void getSubmittedFeedbackCount_WithExistingSubmissions_ShouldReturnCount() {
        // Arrange
        when(submissionRepo.countByUsername("john")).thenReturn(5L);

        // Act
        long count = feedbackService.getSubmittedFeedbackCount("john");

        // Assert
        assertEquals(5L, count);
        verify(submissionRepo).countByUsername("john");
    }

    @Test
    void getSubmittedFeedbackCount_WithNoSubmissions_ShouldReturnZero() {
        // Arrange
        when(submissionRepo.countByUsername("john")).thenReturn(0L);

        // Act
        long count = feedbackService.getSubmittedFeedbackCount("john");

        // Assert
        assertEquals(0L, count);
    }
}
