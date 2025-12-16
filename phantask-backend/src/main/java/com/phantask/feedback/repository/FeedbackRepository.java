package com.phantask.feedback.repository;

import com.phantask.feedback.entity.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {

    // Admin-created feedback forms
    List<FeedbackEntity> findBySubmittedByUsernameIsNull();
    
   Optional<FeedbackEntity> findByIdAndSubmittedByUsernameIsNull(Long id);


    // Prevent duplicate submission
    boolean existsByFeedbackCodeAndSubmittedByUsername(
            String feedbackCode,
            String username
    );

    // Fetch all submissions for report
    List<FeedbackEntity> findByFeedbackCodeAndSubmittedByUsernameIsNotNull(String feedbackCode);
    
    
    //for updation of any feedback if exit by code and not submiited by any user yet
    boolean existsByFeedbackCodeAndSubmittedByUsernameIsNotNull(String feedbackCode);
    
    void deleteByFeedbackCode(String feedbackCode);
    
    
    long countBySubmittedByUsername(String username);


}
