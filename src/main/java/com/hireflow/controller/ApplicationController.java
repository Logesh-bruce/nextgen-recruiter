package com.hireflow.controller;

import com.hireflow.domain.enums.ApplicationStatus;
import com.hireflow.dto.request.CreateApplicationRequest;
import com.hireflow.dto.request.UpdateApplicationStatusRequest;
import com.hireflow.dto.response.ApplicationResponse;
import com.hireflow.dto.response.ApplicationSummaryResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.application.ApplicationService;
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
import java.util.UUID;

/**
 * REST controller for candidate application submissions and recruiter applicant pipeline management.
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Endpoints for submitting applications, tracking status, and reviewing applicants")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit a job application")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody CreateApplicationRequest request) {

        ApplicationResponse response = applicationService.submitApplication(currentUser.getId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/applications/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Get applications submitted for recruiter's jobs (filterable by job ID and status)")
    public ResponseEntity<Page<ApplicationSummaryResponse>> getRecruiterApplications(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ApplicationSummaryResponse> page = applicationService.getRecruiterApplications(currentUser.getId(), jobId, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get application history for the logged-in candidate")
    public ResponseEntity<Page<ApplicationSummaryResponse>> getMyApplications(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ApplicationSummaryResponse> page = applicationService.getMyApplications(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get details of a specific application (accessible by candidate applicant or job recruiter)")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        ApplicationResponse response = applicationService.getApplicationById(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update application status (SHORTLISTED, REJECTED, REVIEWING) and recruiter notes")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {

        ApplicationResponse response = applicationService.updateApplicationStatus(id, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw an active application")
    public ResponseEntity<ApplicationResponse> withdrawApplication(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        ApplicationResponse response = applicationService.withdrawApplication(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
