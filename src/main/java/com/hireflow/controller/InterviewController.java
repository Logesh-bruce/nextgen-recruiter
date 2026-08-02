package com.hireflow.controller;

import com.hireflow.domain.enums.InterviewStatus;
import com.hireflow.dto.request.CreateInterviewRequest;
import com.hireflow.dto.request.UpdateInterviewRequest;
import com.hireflow.dto.response.InterviewQuestionResponse;
import com.hireflow.dto.response.InterviewResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.interview.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for scheduling, managing, and viewing candidate interviews.
 */
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Endpoints for scheduling, updating, and conducting candidate interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Schedule a new candidate interview")
    public ResponseEntity<InterviewResponse> scheduleInterview(
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody CreateInterviewRequest request) {

        InterviewResponse response = interviewService.scheduleInterview(currentUser.getId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/interviews/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of interviews (filterable by applicationId or status)")
    public ResponseEntity<Page<InterviewResponse>> getInterviews(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestParam(required = false) UUID applicationId,
            @RequestParam(required = false) InterviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<InterviewResponse> interviews = interviewService.getInterviews(currentUser.getId(), applicationId, status, pageable);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get details of a specific interview")
    public ResponseEntity<InterviewResponse> getInterviewById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        InterviewResponse response = interviewService.getInterviewById(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update or reschedule an existing interview")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestBody UpdateInterviewRequest request) {

        InterviewResponse response = interviewService.updateInterview(id, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cancel an interview")
    public ResponseEntity<Void> cancelInterview(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        interviewService.cancelInterview(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/questions")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get AI-generated interview questions for an interview session")
    public ResponseEntity<List<InterviewQuestionResponse>> getInterviewQuestions(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        List<InterviewQuestionResponse> questions = interviewService.getInterviewQuestions(id, currentUser.getId());
        return ResponseEntity.ok(questions);
    }
}
