package com.phantask.feedback.controller;

import com.phantask.feedback.dto.CreateFeedbackDto;
import com.phantask.feedback.dto.FeedbackReportDto;
import com.phantask.feedback.dto.SubmitFeedbackDto;
import com.phantask.feedback.entity.FeedbackEntity;
import com.phantask.feedback.service.FeedbackService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    /* ================= ADMIN endpoints ================= */

    @PostMapping("/admin/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> createFeedback(
            @RequestBody @Valid CreateFeedbackDto dto,
            Authentication auth
    ) {
        String admin = auth.getName(); // consistent with Task
        service.createFeedback(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/report/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<FeedbackReportDto> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReport(id));
    }
    
    
    @PutMapping("/admin/update/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody CreateFeedbackDto dto
    ) {
        service.updateFeedback(id, dto);
        return ResponseEntity.ok().build();
    }
    
    
    
    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
    	System.out.println("DELETE CONTROLLER HIT");

        service.deleteFeedback(id);
        return ResponseEntity.ok().build();
    }
    
    
    
    
    
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<FeedbackEntity>> getAll() {
        return ResponseEntity.ok(service.getAllFeedbackTemplates());
    }

    /* ================= USER endpoints ================= */

    @GetMapping("/user/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FeedbackEntity>> getAvailableFeedback(
            Authentication auth
    ) {
        String username = auth.getName();
        List<String> roles = getRolesFromAuth(auth);

        return ResponseEntity.ok(
                service.getAvailableFeedbackForUser(username, roles)
        );
    }

    @PostMapping("/user/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable Long id,
            @RequestBody @Valid SubmitFeedbackDto dto,
            Authentication auth
    ) {
        String username = auth.getName();
        List<String> roles = getRolesFromAuth(auth);

        service.submitFeedback(id, dto, username, roles);
        return ResponseEntity.ok().build();
    }
    
    
    
    @GetMapping("/user/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> mySubmissionCount(Authentication auth) {
        return ResponseEntity.ok(
                service.getSubmittedFeedbackCount(auth.getName())
        );
    }

    /* ================= HELPER (same as Task) ================= */

    private List<String> getRolesFromAuth(Authentication auth) {
        if (auth == null) return Collections.emptyList();

        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
