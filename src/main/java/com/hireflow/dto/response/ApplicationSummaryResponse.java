package com.hireflow.dto.response;

import com.hireflow.domain.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Concise Application summary DTO for recruiter applicant list endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryResponse {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private CandidateSummaryResponse candidate;
    private ApplicationStatus status;
    private Integer matchScore;
    private Instant appliedAt;
}
