package com.hireflow.controller;

import com.hireflow.dto.response.ParseStatusResponse;
import com.hireflow.dto.response.ResumeResponse;
import com.hireflow.dto.response.ResumeUploadResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.resume.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for resume file uploads, background parsing status, and details.
 */
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Endpoints for uploading resumes (PDF/DOCX), parsing status, and candidate CV management")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload a PDF or DOCX resume (triggers background parsing pipeline)")
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {

        ResumeUploadResponse response = resumeService.uploadResume(currentUser.getId(), file, isPrimary);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get list of resumes uploaded by the logged-in candidate")
    public ResponseEntity<List<ResumeResponse>> getCandidateResumes(
            @AuthenticationPrincipal SecurityUser currentUser) {

        List<ResumeResponse> resumes = resumeService.getCandidateResumes(currentUser.getId());
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get detailed information and extracted text for a specific resume")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        ResumeResponse response = resumeService.getResumeById(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/parse-status")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Check asynchronous parsing status of an uploaded resume")
    public ResponseEntity<ParseStatusResponse> getParseStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        ParseStatusResponse status = resumeService.getParseStatus(id, currentUser.getId());
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete an uploaded resume")
    public ResponseEntity<Void> deleteResume(
            @PathVariable UUID id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        resumeService.deleteResume(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
