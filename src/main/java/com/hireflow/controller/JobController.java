package com.hireflow.controller;

import com.hireflow.domain.enums.JobStatus;
import com.hireflow.dto.request.CreateJobRequest;
import com.hireflow.dto.request.UpdateJobStatusRequest;
import com.hireflow.dto.response.JobResponse;
import com.hireflow.dto.response.JobSummaryResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.job.JobService;
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
 * REST controller for job posting management, search, and status transitions.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Endpoints for job postings, listings, search, and recruitment management")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Get active job listings with search and filter parameters")
    public ResponseEntity<Page<JobSummaryResponse>> getActiveJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean isRemote,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<JobSummaryResponse> jobs = jobService.getActiveJobs(search, location, isRemote, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get job postings belonging to the logged-in recruiter")
    public ResponseEntity<Page<JobSummaryResponse>> getMyJobs(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestParam(required = false) JobStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<JobSummaryResponse> jobs = jobService.getMyJobs(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information for a specific job posting")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        UUID currentUserId = currentUser != null ? currentUser.getId() : null;
        JobResponse job = jobService.getJobById(id, currentUserId);
        return ResponseEntity.ok(job);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Create a new job posting")
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody CreateJobRequest request) {

        JobResponse createdJob = jobService.createJob(currentUser.getId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/jobs/" + createdJob.getId()))
                .body(createdJob);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update an existing job posting")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody CreateJobRequest request) {

        JobResponse updatedJob = jobService.updateJob(id, currentUser.getId(), request);
        return ResponseEntity.ok(updatedJob);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Update the status of a job posting (DRAFT, ACTIVE, PAUSED, CLOSED)")
    public ResponseEntity<JobResponse> updateJobStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser,
            @Valid @RequestBody UpdateJobStatusRequest request) {

        JobResponse updatedJob = jobService.updateJobStatus(id, currentUser.getId(), request);
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Delete a job posting")
    public ResponseEntity<Void> deleteJob(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        jobService.deleteJob(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
