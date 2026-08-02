package com.hireflow.controller;

import com.hireflow.dto.request.GenerateQuestionsRequest;
import com.hireflow.dto.response.ApplicantRankingResponse;
import com.hireflow.dto.response.GeneratedQuestionsResponse;
import com.hireflow.dto.response.MatchScoreResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.ai.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller exposing internal AI endpoints for match scoring, question generation, and candidate rankings.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Services", description = "Endpoints for AI-powered resume-job match scoring, skill-gap analysis, and question generation")
public class AiController {

    private final AiService aiService;

    @GetMapping("/applications/{applicationId}/score")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Get AI match score and skill-gap analysis for an application (cached)")
    public ResponseEntity<MatchScoreResponse> getApplicationScore(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal SecurityUser currentUser) {

        MatchScoreResponse response = aiService.getApplicationScore(applicationId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/{applicationId}/score/refresh")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Force re-calculation of AI match score (bypasses cache)")
    public ResponseEntity<Map<String, String>> refreshApplicationScore(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal SecurityUser currentUser) {

        aiService.refreshApplicationScore(applicationId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Re-scoring triggered successfully."));
    }

    @PostMapping("/generate-questions")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "On-demand AI interview question generation")
    public ResponseEntity<GeneratedQuestionsResponse> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request) {

        GeneratedQuestionsResponse response = aiService.generateQuestions(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{jobId}/applicant-ranking")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get ranked leaderboard of applicants for a job based on AI match score")
    public ResponseEntity<List<ApplicantRankingResponse>> getApplicantRanking(
            @PathVariable UUID jobId,
            @RequestParam(required = false) Integer minScore,
            @AuthenticationPrincipal SecurityUser currentUser) {

        List<ApplicantRankingResponse> response = aiService.getApplicantRanking(jobId, currentUser.getId(), minScore);
        return ResponseEntity.ok(response);
    }
}
